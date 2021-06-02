package com.rushil.voicerestaurant

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class Session(cntx:Context) {
    private val prefs: SharedPreferences
    fun setuseId(usename: String?) {
        prefs.edit().putString("usename", usename).commit()
    }


    fun getuseId(): String? {
        return prefs.getString("usename", "")
    }
    fun setStatus(rstatus:Boolean){
        prefs.edit().putString("status", rstatus.toString()).commit()
    }
    fun getStatus(): String? {
        return prefs.getString("status", "")
    }

    init {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx)
    }
}