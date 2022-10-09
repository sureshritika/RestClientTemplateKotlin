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
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import okhttp3.Headers
import org.json.JSONObject


class TweetDetails : AppCompatActivity() {

    lateinit var tweet : Tweet
    private lateinit var profileImage : ImageView
    private lateinit var name : TextView
    private lateinit var username : TextView
    private lateinit var tweetBody : TextView
    private lateinit var bodyImg : ImageView
    private lateinit var bodyVideo : VideoView
    private lateinit var time : TextView
    private lateinit var date : TextView
    private lateinit var likeSwitch : TextSwitcher
    private lateinit var likeBtn : ImageButton
    private lateinit var retweetSwitch : TextSwitcher
    private lateinit var verified : ImageView
    private lateinit var videoLoading : ProgressBar

    private lateinit var replyingToText : TextView
    private lateinit var meImage : ImageView
    private lateinit var replyEdit : EditText
    private lateinit var replyCount : TextView
    private lateinit var replyBtn : Button
    var replyContent : String = ""

    lateinit var client : TwitterClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tweet_details)

        setSupportActionBar(findViewById(R.id.id_tweetBar))
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);

        client = TwitterApplication.getRestClient(this)

        profileImage = findViewById(R.id.id_profileImage)
        name = findViewById(R.id.id_name)
        username = findViewById(R.id.id_username)
        tweetBody = findViewById(R.id.id_tweetBody)
        time = findViewById(R.id.id_time)
        date = findViewById(R.id.id_date)
        likeSwitch = findViewById(R.id.id_likeSwitch)
        likeBtn = findViewById(R.id.id_likeBtn)
        retweetSwitch = findViewById(R.id.id_retweetSwitch)
        verified = findViewById(R.id.id_verified)
        bodyImg = findViewById(R.id.id_bodyImg)
        bodyVideo = findViewById(R.id.id_bodyVideo)
        videoLoading = findViewById(R.id.id_videoLoading)

        replyingToText = findViewById(R.id.id_replyingToText)
        meImage = findViewById(R.id.id_meImage)
        replyEdit = findViewById(R.id.id_replyEdit)
        replyCount = findViewById(R.id.id_replyCount)
        replyBtn = findViewById(R.id.id_replyBtn)

        tweet = intent.getParcelableExtra("TWEET_EXTRA") as Tweet

        TWEET_ID = tweet.id
        TWEET_USERNAME = tweet.user?.screenName.toString()

        Log.d("RITIKA" , "tweet clicked on ${tweet.id}")

        Glide.with(this).load(TimelineActivity.DP_IMG_URL).apply(RequestOptions.circleCropTransform()).into(meImage)

        Glide.with(this).load(tweet.user?.publicImageUrl).apply(RequestOptions.circleCropTransform()).into(profileImage)
        name.text = tweet.user?.name
        username.text = "@" + tweet.user?.screenName
        if (tweet.user?.verified == true)
            Glide.with(this).load(R.drawable.verified).into(verified)
        else
            verified.setImageDrawable(null)
        tweetBody.text = tweet.body
        time.text = tweet.getTime()
        date.text = tweet.getDate()
        likeSwitch.setText(tweet.likes.toString())
        likeBtn.isSelected = tweet.liked
        retweetSwitch.setText(tweet.retweets.toString())

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

        if (tweet.user?.verified == true)
            Glide.with(this).load(R.drawable.verified).into(verified)
        Log.d("RITIKA" , "source: ${tweet.source}")
        if (tweet.mediaType == "photo")
            Glide.with(this).load(tweet.mediaUrl).into(bodyImg)
        if (tweet.mediaType == "video")
        {
            videoLoading.visibility = View.VISIBLE
            bodyVideo.setVideoURI(Uri.parse(tweet.mediaUrl))
            val mediaController = MediaController(this)
            mediaController.setAnchorView(bodyVideo)
            mediaController.setMediaPlayer(bodyVideo)
            bodyVideo.setMediaController(mediaController)
            bodyVideo.setZOrderOnTop(true)
            bodyVideo.setOnPreparedListener(OnPreparedListener {
                videoLoading.visibility = View.GONE
                bodyVideo.start()
            })
        }
        else
            bodyVideo.visibility = View.GONE



        replyEdit.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                replyingToText.visibility = View.VISIBLE
                replyingToText.text = "Replying to @${tweet.user?.screenName}"
                replyCount.visibility = View.VISIBLE
            }
            if (!hasFocus) {
                replyingToText.visibility = View.GONE
                replyCount.visibility = View.GONE
            }
        })



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

                    val snackbar = Snackbar.make(it, "", Snackbar.LENGTH_LONG)
                    val customSnackView: View = layoutInflater.inflate(R.layout.fragment_reply_success, null)
                    val snackbarLayout = snackbar.view as SnackbarLayout
                    snackbarLayout.setBackgroundColor(Color.TRANSPARENT)
                    val params = snackbarLayout.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.TOP
                    snackbarLayout.layoutParams = params
                    snackbarLayout.addView(customSnackView, 0);
                    snackbar.show();
                    replyEdit.setText("")
                    replyEdit.clearFocus()
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

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("tweet" , tweet)
        Log.d("RITIKA" , "back pressed ${tweet.body}")
        setResult(RESULT_OK , intent)
        super.onBackPressed()
    }

    companion object {
        var TWEET_ID : Long = 0
        var TWEET_USERNAME : String = ""
    }
}