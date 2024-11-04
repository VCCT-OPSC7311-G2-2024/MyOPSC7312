package com.example.myopsc7312

import android.content.Context
import android.app.Application
import android.content.res.Configuration
import java.util.Locale

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        setLocale(this)
    }
    //Please work
    companion object {
        fun setLocale(context: Context) {
            val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
            val languageCode = sharedPreferences.getString("languageCode", "en") ?: "en"
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
}