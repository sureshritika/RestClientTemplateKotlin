package com.codepath.apps.restclienttemplate

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codepath.apps.restclienttemplate.models.Tweet

class TweetDetails : AppCompatActivity() {

    private lateinit var profileImage : ImageView
    private lateinit var name : TextView
    private lateinit var username : TextView
    private lateinit var tweetBody : TextView
    private lateinit var time : TextView
    private lateinit var date : TextView
    private lateinit var likes : TextView
    private lateinit var retweets : TextView
    private lateinit var verified : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tweet_details)

        profileImage = findViewById(R.id.id_profileImage)
        name = findViewById(R.id.id_name)
        username = findViewById(R.id.id_username)
        tweetBody = findViewById(R.id.id_tweetBody)
        time = findViewById(R.id.id_time)
        date = findViewById(R.id.id_date)
        likes = findViewById(R.id.id_likeTxt)
        retweets = findViewById(R.id.id_retweetTxt)
        verified = findViewById(R.id.id_verified)

        val tweet = intent.getSerializableExtra("TWEET_EXTRA") as Tweet

        Glide.with(this).load(tweet.user?.publicImageUrl).apply(RequestOptions.circleCropTransform()).into(profileImage)
        name.text = tweet.user?.name
        username.text = "@" + tweet.user?.screenName
        tweetBody.text = tweet.body
        time.text = tweet.getTime()
        date.text = tweet.getDate()
        likes.text = tweet.likes.toString()
        retweets.text = tweet.retweets.toString()
        if (tweet.user?.verified == true)
            Glide.with(this).load(R.drawable.verified).into(verified)
        Log.d("RITIKA" , "source: ${tweet.source}")

    }
}