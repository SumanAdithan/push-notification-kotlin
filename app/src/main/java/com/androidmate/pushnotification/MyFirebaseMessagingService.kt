package com.androidmate.pushnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
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

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE
        else 0

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


        val channelId = "default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title ?: "FCM")
            .setContentText(message ?: "")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .addAction(0, "ACCEPT", acceptPendingIntent)
            .addAction(0, "REJECT", rejectPendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        Log.d("notificationId: ","notificationId: $notificationId")

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

}