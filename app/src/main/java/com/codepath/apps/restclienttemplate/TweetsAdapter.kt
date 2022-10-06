package com.codepath.apps.restclienttemplate

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codepath.apps.restclienttemplate.models.Tweet

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
        private val timeStamp = itemView.findViewById<TextView>(R.id.id_timeStamp)
        private val likes = itemView.findViewById<TextView>(R.id.id_likeTxt)
        private val retweets = itemView.findViewById<TextView>(R.id.id_retweetTxt)
        private val verified = itemView.findViewById<ImageView>(R.id.id_verified)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(tweet : Tweet) {
            name.text = tweet.user?.name
            username.text = "@" + tweet.user?.screenName
            tweetBody.text = tweet.body
            timeStamp.text = tweet.getFormattedTimestamp()
            Glide.with(context).load(tweet.user?.publicImageUrl).apply(RequestOptions.circleCropTransform()).into(profileImage)
            likes.text = tweet.likes.toString()
            retweets.text = tweet.retweets.toString()
            if (tweet.user?.verified == true)
                Glide.with(context).load(R.drawable.verified).into(verified)
        }

        override fun onClick(view: View?) {
            val tweet = tweets[absoluteAdapterPosition]
            tweet.writeObject()
            tweet.readObject()
            val intent = Intent(context , TweetDetails::class.java)

            intent.putExtra("TWEET_EXTRA" , tweet)
            context.startActivity(intent)

        }

    }

}
