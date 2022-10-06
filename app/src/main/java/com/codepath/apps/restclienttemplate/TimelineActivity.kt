package com.codepath.apps.restclienttemplate

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.models.EndlessRecyclerViewScrollListener
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import org.json.JSONException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter


class TimelineActivity : AppCompatActivity() {

    lateinit var client : TwitterClient
    lateinit var tweetsRV : RecyclerView
    lateinit var adapter : TweetsAdapter
    lateinit var progress : ProgressBar
    lateinit var swipeContainer : SwipeRefreshLayout
    lateinit var scrollListener : EndlessRecyclerViewScrollListener

    val tweets = ArrayList<Tweet>()
    var maxId : Long = Long.MAX_VALUE
    var count : Int = 10;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        /*
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setCustomView(layoutInflater.inflate(R.layout.twitter_bar, null))
        val view: View = layoutInflater.inflate(R.layout.twitter_bar, null)
        val layoutParams = ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        supportActionBar!!.setCustomView(view, layoutParams)
        val parent = view.parent as androidx.appcompat.widget.Toolbar
        parent.setContentInsetsAbsolute(0, 0)
        */

        client = TwitterApplication.getRestClient(this)

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

    fun populateHomeTimeline() {
        client.getHomeTimeline(object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i("RITIKA" , "populateHomeTimeline success: ${json}")
                
                val jsonArray = json.jsonArray
                Log.d("RITIKA" , "jsonArray: ${jsonArray[0]}")

                try {
                    // Clear out our currently fetched tweets
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)

                    tweets.clear()
                    tweets.addAll(listOfNewTweetsRetrieved)
                    for (i in 0 until tweets.size) {
                        val tweet = tweets.get(i)
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


}