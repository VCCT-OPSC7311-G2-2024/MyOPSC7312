package com.example.myopsc7312

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat


//Class  for handling notifications
class NotificationHelper(private val context: Context) {

    fun createNotification(title: String, message: String) {
        val channelId = "notification_channel"
        val channelName = "myChannel"

        var builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_icon)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)

        builder = builder.setContent(getRemoteView(context, title, message))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(0, builder.build())
    }

     fun getRemoteView(context: Context,title: String, message: String): RemoteViews {
        val remoteView = RemoteViews(context.packageName, R.layout.notification)
        remoteView.setTextViewText(R.id.title, title)
        remoteView.setTextViewText(R.id.description, message)
        remoteView.setImageViewResource(R.id.app_logo, R.drawable.app_icon)
        return remoteView
    }
}