package com.example.myopsc7312

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

object NotificationUtils {
    fun sendNotification(topic: String, title: String, message: String) {
        val remoteMessage = RemoteMessage.Builder("/topics/$topic")
            .setMessageId(System.currentTimeMillis().toString())
            .addData("title", title)
            .addData("body", message)
            .build()

        FirebaseMessaging.getInstance().send(remoteMessage)
    }
}