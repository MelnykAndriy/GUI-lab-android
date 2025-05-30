package com.msgtrik.msgtrik.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

    fun saveAccessToken(token: String) {
        prefs.edit().putString(Constants.KEY_ACCESS_TOKEN, token).apply()
    }

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(Constants.KEY_REFRESH_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(Constants.KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(Constants.KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens() {
        prefs.edit().apply {
            remove(Constants.KEY_ACCESS_TOKEN)
            remove(Constants.KEY_REFRESH_TOKEN)
            apply()
        }
    }
} 