package com.codepath.apps.restclienttemplate

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.apps.restclienttemplate.models.User
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import okhttp3.Headers
import org.json.JSONObject
import java.sql.Time


class CommentLayout : AppCompatActivity() {

    lateinit var tweet : Tweet
    lateinit var client : TwitterClient
    private lateinit var profileImage : ImageView
    private lateinit var name : TextView
    private lateinit var username : TextView
    private lateinit var verified : ImageView
    private lateinit var replyingToText : TextView
    private lateinit var replyEdit : EditText
    private lateinit var replyCount : TextView
    private lateinit var replyBtn : Button

    var replyContent : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_comment)

        client = TwitterApplication.getRestClient(this)

        profileImage = findViewById(R.id.id_profileImage)
        name = findViewById(R.id.id_name)
        username = findViewById(R.id.id_username)
        verified = findViewById(R.id.id_verified)
        replyEdit = findViewById(R.id.id_replyEdit)
        replyCount = findViewById(R.id.id_replyCount)
        replyBtn = findViewById(R.id.id_replyBtn)

        tweet = intent.getParcelableExtra("TWEET_EXTRA") as Tweet

        replyingToText = findViewById(R.id.id_replyingToText)
        replyingToText.text = "Replying to @${tweet.user?.screenName}"

        TWEET_ID = tweet.id
        TWEET_USERNAME = tweet.user?.screenName.toString()

        Log.d("RITIKA" , "tweet clicked on ${tweet.id}")

        Glide.with(this).load(TimelineActivity.DP_IMG_URL).apply(RequestOptions.circleCropTransform()).into(profileImage)
        name.text = TimelineActivity.ME_NAME
        username.text = "@" + TimelineActivity.ME_USERNAME
        if (TimelineActivity.ME_VERIFIED == true)
            Glide.with(this).load(R.drawable.verified).into(verified)
        else
            verified.setImageDrawable(null)

        replyEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
                replyCount.text = ""+p0.length+"/280"
                if (p0.length > 280) {
                    replyCount.setTextColor(Color.RED)
                    replyBtn.isClickable = false
                    replyBtn.alpha = 0.5F
                }
                else if (p0.isEmpty()) {
                    replyBtn.isClickable = false
                    replyBtn.alpha = 0.5F
                }
                else
                {
                    replyCount.setTextColor(Color.DKGRAY)
                    replyBtn.isClickable = true
                    replyBtn.alpha = 1F
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        replyBtn.setOnClickListener {
            replyContent= replyEdit.text.toString()

            Log.d("RITIKA" , "reply btn clicked")
            client.reply(object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                    Log.d("RITIKA" , "replybtn success")
                    Log.d("RITIKA" , "replied $json")

                    tweet = Tweet.fromJson(json.jsonObject)

                    val intent = Intent()
                    intent.putExtra("tweet" , tweet)
                    Log.d("RITIKA" , "back pressed ${tweet.body}")
                    setResult(RESULT_OK , intent)
                    finish()

                    TweetsAdapter.REPLY_CODE = 1
                }

                override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?
                ) {
                    val code = JSONObject(response).getJSONArray("errors").getJSONObject(0).getInt("code")
                    Log.d("RITIKA" , "replybtn failure")
                    Log.d("RITIKA" , "reply faiure response $response")
                }

            } , " @${TWEET_USERNAME} $replyContent" , TWEET_ID)
        }

    }

    companion object {
        var TWEET_ID : Long = 0
        var TWEET_USERNAME : String = ""
    }
}