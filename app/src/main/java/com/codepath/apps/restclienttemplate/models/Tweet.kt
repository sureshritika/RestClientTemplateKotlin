package com.codepath.apps.restclienttemplate.models


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

@Parcelize
class Tweet(
    var body: String = "",
    var createdAt: String = "",
    var user: User? = null,
    var id: Long = 0,
    var likes: Int = 0,
    var retweets: Int = 0,
    var liked: Boolean = false,
    var retweeted: Boolean = false,
    var source: String = "",
    var mediaType: String = "",
    var mediaUrl: String = "",
    var inReplyToScreenName : String = "" ,

    ) : Parcelable {

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
            if (jsonObject.has("extended_entities")) {
                tweet.mediaType = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("type")
                if (tweet.mediaType == "video")
                    tweet.mediaUrl = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getJSONObject("video_info").getJSONArray("variants").getJSONObject(0).getString("url")
                else if (tweet.mediaType == "photo")
                    tweet.mediaUrl = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("media_url_https")
            }
            tweet.inReplyToScreenName = jsonObject.getString("in_reply_to_screen_name")
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
}