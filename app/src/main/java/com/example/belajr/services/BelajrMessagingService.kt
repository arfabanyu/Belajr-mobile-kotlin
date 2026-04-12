package com.example.belajr.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.belajr.R
import com.example.belajr.FriendRequestActivity
import com.example.belajr.ChatActivity

class BelajrMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val rawTitle = message.notification?.title ?: message.data["title"]
        val rawBody = message.notification?.body ?: message.data["body"]
        val type = message.data["type"]
        val senderName = message.data["sender_name"] ?: "Seseorang"

        val (finalTitle, finalBody) = when (type) {
            "friend_request" -> {
                "Permintaan Pertemanan Baru" to "$senderName ingin berteman denganmu di BelaJr!"
            }
            "friend_accept" -> {
                "Permintaan Pertemanan Diterima" to "Sekarang kamu dan $senderName sudah berteman. Ayo mulai belajar bareng!"
            }
            "new_message" -> {
                "Pesan Baru dari $senderName" to (rawBody ?: "Mengirimkan pesan kepadamu.")
            }
            "match_found" -> {
                "Partner Belajar Ditemukan!" to "Hore! Kamu cocok dengan $senderName. Cek profilnya sekarang!"
            }
            else -> {
                (rawTitle ?: "BelaJr") to (rawBody ?: "Ada aktivitas baru untukmu!")
            }
        }

        showNotification(finalTitle, finalBody, type)
    }

    private fun showNotification(title: String, body: String, type: String?) {
        val channelId = when(type) {
            "new_message" -> "chat_notifications"
            else -> "friend_notifications"
        }
        
        val channelName = when(type) {
            "new_message" -> "Pesan Chat"
            else -> "Aktivitas Pertemanan"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk $channelName"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val targetActivity = when (type) {
            "new_message" -> ChatActivity::class.java
            "friend_request" -> FriendRequestActivity::class.java
            else -> FriendRequestActivity::class.java
        }

        val intent = Intent(this, targetActivity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) 
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
