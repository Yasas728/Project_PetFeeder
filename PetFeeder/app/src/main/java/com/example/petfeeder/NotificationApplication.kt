package com.example.petfeeder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class NotificationApplication : Application()  {
    override fun onCreate() {
        super.onCreate()

        val notificationChannel = NotificationChannel(
            "Intruder_Alert_Channel_ID",  // channel ID
            "Intruder Alert",             // channel name
            NotificationManager.IMPORTANCE_HIGH // importance
        )

        notificationChannel.description = "This channel is for Intruder Alert"

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

    }
}