package com.codepath.apps.restclienttemplate

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.media.MediaMuxer
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codepath.apps.restclienttemplate.models.EndlessRecyclerViewScrollListener
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.apps.restclienttemplate.models.User
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import okhttp3.Headers
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text


class TimelineActivity : AppCompatActivity() {

    lateinit var twitterLogo : ImageView
    lateinit var tweetsRV : RecyclerView
    lateinit var adapter : TweetsAdapter
    lateinit var progress : ProgressBar
    lateinit var swipeContainer : SwipeRefreshLayout
    lateinit var scrollListener : EndlessRecyclerViewScrollListener
    lateinit var composeFAB : FloatingActionButton
    lateinit var client : TwitterClient

    val tweets = ArrayList<Tweet>()
    var maxId : Long = Long.MAX_VALUE
    var count : Int = 10
    var tweetContent : String = ""
    lateinit var me : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        val sharedPreference = getSharedPreferences("TWITTER" , Context.MODE_PRIVATE)

        setSupportActionBar(findViewById(R.id.id_twitterBar))
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);

        client = TwitterApplication.getRestClient(this)

        client.getMe(object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                Log.d("RITIKA" , "getMe success ${json}")
                me = User.fromJson(json!!.jsonObject)
                ME_NAME = me?.name
                ME_USERNAME = me?.screenName
                DP_IMG_URL = me?.publicImageUrl
                ME_VERIFIED = me?.verified
            }
            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                Log.d("RITIKA" , "getMe failure ${response}")
            }
        })

        progress = findViewById(R.id.id_progress)

        swipeContainer = findViewById(R.id.id_swipeContainer)
        swipeContainer.setOnRefreshListener {
            Log.i("RITIKA" , "refreshing timeline")
            populateHomeTimeline()
        }

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);

        tweetsRV = findViewById(R.id.id_tweetsRV)
        adapter = TweetsAdapter(tweets , this)

        tweetsRV.layoutManager = LinearLayoutManager(this)
        tweetsRV.adapter = adapter

        twitterLogo = findViewById(R.id.id_twitterLogo)
        twitterLogo.setOnClickListener {
            tweetsRV.smoothScrollToPosition(0)
        }

        composeFAB = findViewById(R.id.id_composeFAB)
        composeFAB.setOnClickListener {
            Log.d("RITIKA" , "ready to compose tweet")

            val view = LayoutInflater.from(this).inflate(R.layout.fragment_compose, null)
            val builder = AlertDialog.Builder(this).setView(view).setCancelable(false)
            val dialog = builder.show()

            var snackbar : Snackbar? = null
            val snackBarLayout = view.findViewById<CoordinatorLayout>(R.id.id_snackbarLayout)

            val composeEdit = view.findViewById<EditText>(R.id.id_composeEdit)
            val composeBtn = view.findViewById<Button>(R.id.id_composeBtn)
            val tweetCount = view.findViewById<TextView>(R.id.id_tweetCount)
            val cancelBtn = view.findViewById<MaterialButton>(R.id.id_cancelBtn)

            val profileImage = view.findViewById<ImageView>(R.id.id_profileImage)
            val name = view.findViewById<TextView>(R.id.id_name)
            val username = view.findViewById<TextView>(R.id.id_username)
            val verified = view.findViewById<ImageView>(R.id.id_verified)

            Log.d("RITIKA" , "image url $DP_IMG_URL")
            Glide.with(this).load(me?.publicImageUrl).apply(RequestOptions.circleCropTransform()).into(profileImage)
            name.text = me?.name
            username.text = "@" + me?.screenName
            if (me?.verified == true)
                Glide.with(this).load(R.drawable.verified).into(verified)
            else
                verified.setImageDrawable(null)

            composeEdit.setText(sharedPreference.getString("saved_tweet" , "") , TextView.BufferType.EDITABLE)

            var editor = sharedPreference.edit()

            composeBtn.setOnClickListener {
                tweetContent = composeEdit.text.toString()

                Log.d("RITIKA" , "compose tweet")
                client.publishTweet(object : JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                        Log.d("RITIKA" , "composeBtn success")

                        val tweet = Tweet.fromJson(json.jsonObject)
                        dialog.hide()
                        editor.putString("saved_tweet" , "")
                        editor.commit()
                        tweets.add(0 , tweet)
                        adapter.notifyItemInserted(0)
                        tweetsRV.smoothScrollToPosition(0)
                    }

                    override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?
                    ) {
                        val code = JSONObject(response).getJSONArray("errors").getJSONObject(0).getInt("code")
                        if (code == 187) {
                            Log.i("RITIKA" , "composeBtn failure")
                            snackbar = Snackbar.make(snackBarLayout, "Whoops! You already said that", Snackbar.LENGTH_INDEFINITE).setBackgroundTint(resources.getColor(R.color.light_red)).setTextColor(Color.BLACK)
                            snackbar?.show()
                        }
                    }

                } , tweetContent)
            }

            if (composeEdit.text.isNotEmpty())
            {
                tweetCount.text = ""+composeEdit.text.length+"/280"
            }
            else
            {
                composeBtn.isClickable = false
                composeBtn.setBackgroundColor(Color.GRAY)
            }

            composeEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
                    tweetCount.text = ""+p0.length+"/280"
                    if (p0.length > 280) {
                        tweetCount.setTextColor(Color.RED)
                        composeBtn.isClickable = false
                        composeBtn.setBackgroundColor(Color.GRAY)
                    }
                    else if (p0.isEmpty()) {
                        composeBtn.isClickable = false
                        composeBtn.setBackgroundColor(Color.GRAY)
                    }
                    else
                    {
                        tweetCount.setTextColor(Color.DKGRAY)
                        composeBtn.isClickable = true
                        composeBtn.setBackgroundColor(resources.getColor(R.color.twitter_blue))
                    }

                    snackbar?.dismiss()

                }

                override fun afterTextChanged(p0: Editable?) {

                }

            })

            cancelBtn.setOnClickListener {
                if (composeEdit.text.length == 0)
                    dialog.hide()
                else {
                    val view1 = LayoutInflater.from(this).inflate(R.layout.fragment_save_tweet, null)
                    val builder1 = AlertDialog.Builder(this).setView(view1).setCancelable(false)
                    val dialog1 = builder1.show()

                    val saveBtn = view1.findViewById<MaterialButton>(R.id.id_saveBtn)
                    val discardBtn = view1.findViewById<MaterialButton>(R.id.id_discardBtn)
                    val cancelBtn = view1.findViewById<MaterialButton>(R.id.id_cancelBtn)

                    val tweet = composeEdit.text.toString()

                    discardBtn.setOnClickListener {
                        Log.d("RITIKA" , "discardBtn clicked")
                        if (tweet.equals(sharedPreference.getString("saved_tweet" , "")))
                        {
                            editor.putString("saved_tweet" , "")
                            editor.commit()
                        }
                        dialog1.hide()
                        dialog.hide()
                    }

                    saveBtn.setOnClickListener {
                        Log.d("RITIKA" , "saveBtn clicked")
                        editor.putString("saved_tweet" , tweet)
                        editor.commit()
                        dialog1.hide()
                        dialog.hide()
                    }

                    cancelBtn.setOnClickListener {
                        Log.d("RITIKA" , "cancelBtn clicked")
                        dialog1.hide()
                    }

                }
            }
        }

        scrollListener = object : EndlessRecyclerViewScrollListener(tweetsRV.layoutManager as LinearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.d("RITIKA" , "onLoadMore")

                progress.setVisibility(View.VISIBLE)
                Handler().postDelayed(Runnable {
                    client.getNextPageOfTweets(object : JsonHttpResponseHandler() {
                        override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                            maxId = Tweet.getMaxID(json.jsonArray , maxId)

                            // Log.d("RITIKA" , "getNextPageOfTweets success: ${Tweet.fromJsonArray(json.jsonArray)}")
                            try {
                                tweets.addAll(Tweet.fromJsonArray(json.jsonArray))
                                count = tweets.size
                                // Log.d("RITIKA" , "getNextPageOfTweets")
                                for (i in 0 until Tweet.fromJsonArray(json.jsonArray).size) {
                                    val tweet = Tweet.fromJsonArray(json.jsonArray).get(i)
                                    //Log.d("RITIKA" , "$i: ${tweet.body.substring(0 , 10)}")
                                }
                                for (i in 0 until tweets.size) {
                                    val tweet = tweets.get(i)
                                    // Log.d("RITIKA" , "$i: ${tweet.body.substring(0 , 10)} ${tweet.id}")
                                }


                                adapter.notifyDataSetChanged()
                                swipeContainer.setRefreshing(false);
                                scrollListener.resetState();
                            } catch (e : JSONException) {
                                Log.e("RITIKA" , "json exception $e")
                            }
                        }
                        override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                            Log.d("RITIKA" , "getNextPageOfTweets failure: $response ; $statusCode")
                        }
                    } , maxId-1)
                    progress.setVisibility(View.GONE)
                } , 1500)
            }
        }
        tweetsRV.addOnScrollListener(scrollListener)

        populateHomeTimeline()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //menuInflater.inflate(R.menu.menu_main , menu)
        menu?.findItem(R.id.id_compose)?.setVisible(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_compose) {
            Log.d("RITIKA" , "ready to compose tweet")
            val intent = Intent(this , ComposeActivity::class.java)
            startActivityForResult(intent , REQUEST_CODE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("RITIKA", "onActivityResult: got the input: $requestCode $resultCode $data")
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            val tweet = data?.getParcelableExtra("tweet") as Tweet
            tweets.add(0 , tweet)
            adapter.notifyItemInserted(0)
            tweetsRV.smoothScrollToPosition(0)
        }

        if (resultCode == RESULT_OK && requestCode == TweetsAdapter.REQUEST_CODE && TweetsAdapter.REPLY_CODE == 0) {
            val tweet = data?.getParcelableExtra("tweet") as Tweet
            Log.d("RITIKA" , "update ${TweetsAdapter.POS} , body: ${tweet.body} , likes: ${tweet.likes} , liked: ${tweet.liked}")
            tweets.removeAt(TweetsAdapter.POS)
            tweets.add(TweetsAdapter.POS , tweet)
            adapter.notifyItemChanged(TweetsAdapter.POS)
        }

        if (resultCode == RESULT_OK && TweetsAdapter.REPLY_CODE == 1) {
            val tweet = data?.getParcelableExtra("tweet") as Tweet
            Log.d("RITIKA" , "replied ${tweet.body} to ${tweet.inReplyToScreenName}")
            tweets.add(0 , tweet)
            adapter.notifyItemInserted(0)
            tweetsRV.smoothScrollToPosition(0)
        }
    }

    fun populateHomeTimeline() {
        client.getHomeTimeline(object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i("RITIKA" , "populateHomeTimeline success: ${json}")
                
                val jsonArray = json.jsonArray
                for (i in 0 until jsonArray.length())
                    Log.d("RITIKA" , "json $i: ${jsonArray[i]}")

                try {
                    // Clear out our currently fetched tweets
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)

                    tweets.clear()
                    tweets.addAll(listOfNewTweetsRetrieved)
                    for (i in 0 until tweets.size) {
                        val tweet = tweets.get(i)
                        Log.d("RITIKA" , "media $i: ${tweet.mediaType}")
                    }
                    maxId = Tweet.getMaxID(jsonArray , maxId)

                    adapter.notifyDataSetChanged()
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                } catch (e : JSONException) {
                    Log.e("RITIKA" , "json exception $e")
                }

            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                Log.i("RITIKA" , "populateHomeTimeline failure: $response ; $statusCode")
            }
        } , count)
    }

    companion object {
        val REQUEST_CODE = 10
        var ME_USERNAME = ""
        var ME_NAME = ""
        var DP_IMG_URL = ""
        var ME_VERIFIED = false
    }
}