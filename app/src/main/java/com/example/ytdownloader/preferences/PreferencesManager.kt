package com.example.ytdownloader.preferences

import android.app.Application
import android.content.SharedPreferences

object PreferencesManager {

    private const val preferencesName = "preferences"
    private const val userKey = "user"
    private const val passwordKey = "password"

    private var application: Application? = null
    private var preferences: SharedPreferences? = null

    fun init(application: Application) {
        if (this.application != null) return
        this.application = application
        this.preferences = application.getSharedPreferences(preferencesName, 0)
    }

    var user: String?
        get() = preferences?.getString(userKey, null)
        set(value) {
            val editor = preferences!!.edit()
            editor.putString(userKey, value)
            editor.apply()
        }

    var password: String?
        get() = preferences?.getString(passwordKey, null)
        set(value) {
            val editor = preferences!!.edit()
            editor.putString(passwordKey, value)
            editor.apply()
        }

    fun setUserAndPassword(user: String, password: String) {
        val editor = preferences!!.edit()
        editor.putString(userKey, user)
        editor.putString(passwordKey, password)
        editor.apply()
    }

}