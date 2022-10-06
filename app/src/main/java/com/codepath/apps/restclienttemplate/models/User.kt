package com.codepath.apps.restclienttemplate.models

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class User : Serializable{
    var name : String = ""
    var screenName : String = ""
    var publicImageUrl : String = ""
    var verified : Boolean = false

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

    @Throws(ClassNotFoundException::class, IOException::class, JSONException::class)
    private fun readObject(ois: ObjectInputStream) {
        name = ois.readUTF()
        screenName = ois.readUTF()
        publicImageUrl = ois.readUTF()
        verified = ois.readBoolean()
    }

    @Throws(IOException::class)
    private fun writeObject(oos: ObjectOutputStream) {
        oos.writeUTF(name)
        oos.writeUTF(screenName)
        oos.writeUTF(publicImageUrl)
        oos.writeBoolean(verified)
    }

}