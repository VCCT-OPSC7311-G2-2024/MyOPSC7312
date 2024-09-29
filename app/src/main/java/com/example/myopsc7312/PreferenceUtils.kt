package com.example.myopsc7312

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtils {
    private const val PREFS_NAME = "MyAppPreferences"
    private const val KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled"

    fun areNotificationsEnabled(context: Context): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }
}