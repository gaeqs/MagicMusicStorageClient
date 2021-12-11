package io.github.gaeqs.ytdownloader.preferences

import android.app.Application
import android.content.SharedPreferences

object PreferencesManager {

    private const val preferencesName = "preferences"
    private const val userKey = "user"
    private const val passwordKey = "password"
    private const val hostKey = "host"
    private const val portKey = "port"

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

    var host: String?
        get() = preferences?.getString(hostKey, "localhost")
        set(value) {
            val editor = preferences!!.edit()
            editor.putString(hostKey, value)
            editor.apply()
        }

    var port: Int?
        get() = preferences?.getInt(portKey, 22222)
        set(value) {
            val editor = preferences!!.edit()
            if (value == null) {
                editor.remove(portKey)
            } else {
                editor.putInt(portKey, value)
            }
            editor.apply()
        }

    fun setLoginData(user: String, password: String, host: String, port: Int) {
        val editor = preferences!!.edit()
        editor.putString(userKey, user)
        editor.putString(passwordKey, password)
        editor.putString(hostKey, host)
        editor.putInt(portKey, port)
        editor.apply()
    }

}