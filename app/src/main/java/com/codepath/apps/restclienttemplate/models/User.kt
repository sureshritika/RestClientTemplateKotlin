package com.codepath.apps.restclienttemplate.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

@Parcelize
class User (
    var name : String = "" ,
    var screenName : String = "" ,
    var publicImageUrl : String = "" ,
    var verified : Boolean = false ,
): Parcelable {
    companion object {
        fun fromJson(jsonObject: JSONObject) : User {
            val user = User()
            user.name = jsonObject.getString("name")
            user.screenName = jsonObject.getString("screen_name")
            user.publicImageUrl = jsonObject.getString("profile_image_url_https")
            user.verified = jsonObject.getBoolean("verified")
            return user
        }
    }

}