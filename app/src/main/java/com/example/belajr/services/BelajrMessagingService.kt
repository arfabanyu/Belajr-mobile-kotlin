package com.example.belajr.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.belajr.R

class BelaJrMessagingService : FirebaseMessagingService() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: "BelaJr"
        val body = message.notification?.body ?: return
        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(title: String, body: String) {
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

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
//```
//
//Gw ganti `R.drawable.ic_notification` jadi `R.mipmap.ic_launcher` dulu karena `ic_notification` belum tentu ada di project lu. Nanti kalau mau pakai icon custom, tinggal tambah drawable-nya dan ganti referensinya.
//
//*Soal `index.ts` dan TypeScript*
//
//Iya, Edge Function Supabase memang pakai TypeScript dan dijalankan di atas Deno. Cara buatnya:
//
//1. Install Supabase CLI dulu di laptop lu via PowerShell:
//```
//scoop install supabase
//```
//atau download langsung dari `github.com/supabase/cli/releases`.
//
//2. Setelah ter-install, jalankan di folder project lu:
//```
//supabase init
//supabase functions new notify-friend-request
//```
//
//3. Nanti otomatis terbuat folder `supabase/functions/notify-friend-request/index.ts`. Isi dengan kode Edge Function yang gw kasih sebelumnya.
//
//4. Deploy ke Supabase:
//```
//supabase functions deploy notify-friend-request