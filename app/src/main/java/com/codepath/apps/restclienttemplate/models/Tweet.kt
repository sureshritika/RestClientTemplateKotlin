package com.codepath.apps.restclienttemplate.models


import android.text.Html
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*


class Tweet() : Serializable {

    @Transient
    private var serTweet : ByteArray? = null
    private var tweet : Tweet? = null

    var body : String = ""
    var createdAt : String = ""
    var user : User? = null
    var id : Long = 0
    var likes : Int = 0
    var retweets : Int = 0
    var liked : Boolean = false
    var retweeted : Boolean = false
    var source : String = ""

    companion object {
        fun fromJson(jsonObject: JSONObject) : Tweet {
            val tweet = Tweet()
            tweet.body = jsonObject.getString("text")
            tweet.createdAt = jsonObject.getString("created_at")
            tweet.user = User.fromJson(jsonObject.getJSONObject("user"))
            tweet.id = jsonObject.getLong("id")
            tweet.likes = jsonObject.getInt("favorite_count")
            tweet.retweets = jsonObject.getInt("retweet_count")
            tweet.liked = jsonObject.getBoolean("favorited")
            tweet.retweeted = jsonObject.getBoolean("retweeted")
            tweet.source = jsonObject.getString("entities")
            return tweet
        }

        fun fromJsonArray(jsonArray : JSONArray) : List<Tweet> {
            val tweets = ArrayList<Tweet>()
            for (i in 0 until jsonArray.length()) {
                val json = fromJson(jsonArray.getJSONObject(i))
                tweets.add(json)
            }
            return tweets
        }

        fun getMaxID(jsonArray : JSONArray , maxId : Long) : Long{
            var res : Long = maxId
            val tweets = fromJsonArray(jsonArray)
            for (i in 0 until tweets.size) {
                val tweet = tweets.get(i)
                if (tweet.id < maxId) {
                    res = tweet.id
                }
            }
            return res
        }

    }

    fun getFormattedTimestamp(): String {
        return TimeFormatter.getTimeDifference(createdAt)
    }

    fun getTime(): String {
        return TimeFormatter.getTime(createdAt)
    }

    fun getDate(): String {
        return TimeFormatter.getDate(createdAt)
    }

    fun writeObject() {
        val bos = ByteArrayOutputStream()
        val os = ObjectOutputStream(bos)

        os.writeObject(this)
        os.flush()
        serTweet = bos.toByteArray()
        os.close()
    }

    fun readObject() {
        val bis = ByteArrayInputStream(serTweet)
        val oInputStream = ObjectInputStream(bis)

        tweet = oInputStream.readObject() as Tweet
        oInputStream.close()
    }

    @Throws(IOException::class)
    private fun writeObject(oos: ObjectOutputStream) {
        oos.defaultWriteObject()
    }

    @Throws(ClassNotFoundException::class, IOException::class, JSONException::class)
    private fun readObject(ois: ObjectInputStream) {
        ois.defaultReadObject()
    }

}