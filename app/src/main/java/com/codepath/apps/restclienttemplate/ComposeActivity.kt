package com.codepath.apps.restclienttemplate

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.snackbar.Snackbar
import okhttp3.Headers

class ComposeActivity : AppCompatActivity() {

    lateinit var composeEdit : EditText
    lateinit var composeBtn : Button
    lateinit var tweetCount : TextView

    lateinit var client : TwitterClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_compose)

        composeEdit = findViewById(R.id.id_composeEdit)
        composeBtn = findViewById(R.id.id_composeBtn)
        tweetCount = findViewById(R.id.id_tweetCount)

        client = TwitterApplication.getRestClient(this)


        composeBtn.setOnClickListener {
            val tweetContent = composeEdit.text.toString()
            if (tweetContent.isEmpty()) {
                //Toast.makeText(this , "Empty tweets not allowed!" , Toast.LENGTH_SHORT).show()
                Snackbar.make(it , "Empty tweets not allowed!" , Snackbar.LENGTH_SHORT).show()
            }

            if (tweetContent.length > 280) {
                //Toast.makeText(this , "Tweet is too long! Limit is 280 characters." , Toast.LENGTH_SHORT).show()
                Snackbar.make(it , "Tweet is too long! Limit is 280 characters." , Snackbar.LENGTH_SHORT).show()
            } else {
                Log.d("RITIKA" , "compose tweet")
                client.publishTweet(object : JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                        Log.d("RITIKA" , "composeBtn success")

                        val tweet = Tweet.fromJson(json.jsonObject)

                        val intent = Intent()
                        intent.putExtra("tweet" , tweet)
                        setResult(RESULT_OK , intent)
                        finish()
                    }

                    override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?
                    ) {
                        Log.i("RITIKA" , "composeBtn failure: $response ; $statusCode")
                    }

                } , tweetContent)
            }
        }

        composeEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
                tweetCount.text = ""+p0.length+"/280"
                if (p0.length > 280) {
                    tweetCount.setTextColor(Color.RED)
                    composeBtn.isClickable = false
                    composeBtn.setBackgroundColor(Color.GRAY)

                }
                else
                {
                    tweetCount.setTextColor(Color.DKGRAY)
                    composeBtn.isClickable = true
                    composeBtn.setBackgroundColor(resources.getColor(R.color.twitter_blue))
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

    }
}