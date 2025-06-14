package com.androidmate.pushnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message Received: ${remoteMessage.data}")

        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        sendNotification(title, body)
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        // Send token to your server if needed
    }

    private fun sendNotification(title: String?, message: String?) {
        val notificationId = UUID.randomUUID().hashCode()

        val flag = PendingIntent.FLAG_IMMUTABLE

        val acceptIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("FROM_NOTIFICATION", true)
            putExtra("ACTION_TYPE", "ACCEPT")
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val acceptPendingIntent = PendingIntent.getActivity(this, notificationId + 1, acceptIntent, flag)

        val rejectIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("FROM_NOTIFICATION", true)
            putExtra("ACTION_TYPE", "REJECT")
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val rejectPendingIntent = PendingIntent.getActivity(this, notificationId + 2, rejectIntent, flag)


        val channelId = "custom_channel_2025"
        val soundUri = Uri.parse("android.resource://${packageName}/raw/custom_notification")
        Log.d("SoundURI", "Using sound URI: $soundUri")
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title ?: "FCM")
            .setContentText(message ?: "")
            .setAutoCancel(true)
            .setSound(soundUri)
            .addAction(0, "ACCEPT", acceptPendingIntent)
            .addAction(0, "REJECT", rejectPendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val soundAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .build()

//            val channel = NotificationChannel(
//                channelId,
//                "custom_channel_2025",
//                NotificationManager.IMPORTANCE_HIGH // use HIGH if you want sound to always play
//            ).apply {
//                setSound(soundUri, soundAttributes)
//            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel == null) {
                val soundAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val channel = NotificationChannel(
                    channelId,
                    "Call Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    setSound(soundUri, soundAttributes)
                    enableVibration(true)
                }

                notificationManager.createNotificationChannel(channel)
            }
        }


//        notificationManager.createNotificationChannel(channel)

        Log.d("notificationId: ","notificationId: $notificationId")

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

}