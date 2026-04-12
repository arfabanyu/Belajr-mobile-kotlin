package com.example.belajr.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.belajr.R
import com.example.belajr.FriendRequestActivity

class BelajrMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        // Ambil data dari payload (biasanya Supabase mengirim data via 'data' atau 'notification')
        val title = message.notification?.title ?: message.data["title"] ?: "BelaJr"
        val body = message.notification?.body ?: message.data["body"] ?: "Seseorang mengirimkan permintaan pertemanan!"
        val type = message.data["type"] // Contoh: "friend_request"

        showNotification(title, body, type)
    }

    private fun showNotification(title: String, body: String, type: String?) {
        val channelId = "friend_request"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Friend Request",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        // Intent untuk membuka FriendRequestActivity saat notifikasi diklik
        val intent = Intent(this, FriendRequestActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) 
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set intent klik
            .build()

        try {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Tangani jika permission POST_NOTIFICATIONS belum diberikan (Android 13+)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
