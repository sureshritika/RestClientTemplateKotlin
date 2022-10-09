package com.codepath.apps.restclienttemplate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers


class TweetsAdapter(private val tweets : ArrayList<Tweet> , private val context : Context) : RecyclerView.Adapter<TweetsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)

        val view = inflater.inflate(R.layout.item_tweet , parent , false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tweet : Tweet = tweets.get(position)
        holder.bind(tweet)
    }

    override fun getItemCount(): Int {
        return tweets.size
    }

    // Clean all elements of the recycler
    fun clear() {
        tweets.clear()
        notifyDataSetChanged()
    }

    // Add a list of items -- change to type used
    fun addAll(tweetList: List<Tweet>) {
        tweets.addAll(tweetList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) , View.OnClickListener {
        private val profileImage = itemView.findViewById<ImageView>(R.id.id_profileImage)
        private val name = itemView.findViewById<TextView>(R.id.id_name)
        private val username = itemView.findViewById<TextView>(R.id.id_username)
        private val tweetBody = itemView.findViewById<TextView>(R.id.id_tweetBody)
        private val replyingToText = itemView.findViewById<TextView>(R.id.id_replyingToText)
        private val timeStamp = itemView.findViewById<TextView>(R.id.id_timeStamp)
        private val likeBtn = itemView.findViewById<ImageButton>(R.id.id_likeBtn)
        private val likeSwitch = itemView.findViewById<TextSwitcher>(R.id.id_likeSwitch)
        private val replyBtn = itemView.findViewById<ImageButton>(R.id.id_replyBtn)
        private val retweetSwitch = itemView.findViewById<TextSwitcher>(R.id.id_retweetSwitch)
        private val verified = itemView.findViewById<ImageView>(R.id.id_verified)
        private val client = TwitterApplication.getRestClient(context)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(tweet : Tweet) {
            name.text = tweet.user?.name
            username.text = "@" + tweet.user?.screenName
            tweetBody.text = tweet.body
            timeStamp.text = tweet.getFormattedTimestamp()
            Glide.with(context).load(tweet.user?.publicImageUrl).apply(RequestOptions.circleCropTransform()).into(profileImage)
            likeSwitch.setText(tweet.likes.toString())
            likeBtn.isSelected = tweet.liked
            if (!tweet.inReplyToScreenName.equals("null")) {
                replyingToText.visibility = View.VISIBLE
                replyingToText.text = "Replying to @${tweet.inReplyToScreenName}"
            }
            retweetSwitch.setText(tweet.retweets.toString())
            if (tweet.user?.verified == true)
                Glide.with(context).load(R.drawable.verified).into(verified)
            else
                verified.setImageDrawable(null)
            likeBtn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    likeBtn.isSelected = !likeBtn.isSelected
                    tweet.liked = likeBtn.isSelected
                    if (likeBtn.isSelected) {
                        client.likeTweet(object : JsonHttpResponseHandler() {
                            override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                                Log.d("RITIKA", "likeTweet success")
                                val tv = likeSwitch.getCurrentView() as TextView
                                tweet.likes = tv.text.toString().toInt()+1
                                likeSwitch.setText("${tweet.likes}")
                            }
                            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                                Log.d("RITIKA", "likeTweet failure $response")
                            }
                        }, tweet.id)
                    }
                    if (!likeBtn.isSelected) {
                        client.unlikeTweet(object : JsonHttpResponseHandler() {
                            override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                                Log.d("RITIKA", "unlikeTweet success")
                                val tv = likeSwitch.getCurrentView() as TextView
                                tweet.likes = tv.text.toString().toInt()-1
                                likeSwitch.setText("${tweet.likes}")
                            }
                            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                                Log.d("RITIKA", "unlikeTweet failure $response")
                            }
                        }, tweet.id)
                    }
                }
            })
            replyBtn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    val tweet = tweets[absoluteAdapterPosition]
                    val intent = Intent(context , CommentLayout::class.java)
                    intent.putExtra("TWEET_EXTRA" , tweet)
                    (context as Activity).startActivityForResult(intent, REQUEST_CODE)
                }

            })
        }

        override fun onClick(view: View?) {
            POS = absoluteAdapterPosition
            val tweet = tweets[absoluteAdapterPosition]
            val intent = Intent(context , TweetDetails::class.java)
            intent.putExtra("TWEET_EXTRA" , tweet)
            (context as Activity).startActivityForResult(intent, REQUEST_CODE)
        }
    }

    companion object {
        var POS = 0
        val REQUEST_CODE = 11
        var REPLY_CODE = 0
    }

}
