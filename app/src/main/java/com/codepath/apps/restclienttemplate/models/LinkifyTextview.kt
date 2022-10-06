package com.codepath.apps.restclienttemplate.models

import android.content.Context
import android.text.Layout
import android.text.Selection
import android.text.Spannable
import android.text.style.ClickableSpan
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView


class LinkifyTextView : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init() {
        this.autoLinkMask = Linkify.ALL
    }

    /**
     * @Linkify applies to a movementMethod to the textView @LinkMovementMethod. That movement method thought it
     * implements a scrolling vertically method it overrides any other scrolling method the parent has.
     *
     * Although touchEvent can be dispached to the parent, the specific parent ScrollView needed the whole sequence
     * ACTION_DOWN , ACTION_MOVE, ACTION_UP to perform (sweep detection). So the solution to this problem is after
     * applying @Linkify we need to remove the textView's scrolling method and handle the @LinkMovementMethod link
     * detection action in onTouchEvent of the textView.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val widget = this as TextView
        val text: Any = widget.text
        if (text is Spannable) {
            val buffer = text
            val action = event.action
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                var x = event.x.toInt()
                var y = event.y.toInt()
                x -= widget.totalPaddingLeft
                y -= widget.totalPaddingTop
                x += widget.scrollX
                y += widget.scrollY
                val layout: Layout = widget.layout
                val line: Int = layout.getLineForVertical(y)
                val off: Int = layout.getOffsetForHorizontal(line, x.toFloat())
                val link = buffer.getSpans(
                    off, off,
                    ClickableSpan::class.java
                )
                if (link.size != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget)
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(
                            buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(
                                link[0]
                            )
                        )
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun setText(text: CharSequence, type: BufferType) {
        super.setText(text, type)
        this.movementMethod = null
    }
}