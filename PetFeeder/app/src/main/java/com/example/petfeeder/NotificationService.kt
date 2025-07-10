package com.example.petfeeder

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    companion object {
        private const val INTRUDER_NOTIFICATION_ID = 1001
    }

    fun showIntruderAlert(time: String = "") {
        val contentText = if (time.isNotEmpty()) {
            "Intruder detected at $time!"
        } else {
            "Intruder detected!"
        }

        // Use default notification sound
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, "Intruder_Alert_Channel_ID")
            .setContentTitle("🚨 Security Alert")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Using system icon as fallback
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500)) // More noticeable vibration
            .setSound(defaultSoundUri) // Add sound
            .setOnlyAlertOnce(false) // Allow multiple alerts
            .build()

        notificationManager?.notify(INTRUDER_NOTIFICATION_ID, notification)
    }

    fun cancelIntruderAlert() {
        notificationManager?.cancel(INTRUDER_NOTIFICATION_ID)
    }
}