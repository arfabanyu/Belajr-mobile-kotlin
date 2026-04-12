package com.example.belajr

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.belajr.views.FriendViewModel
import com.example.belajr.views.MessageViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

object NavigationUtils {

    fun setupBottomNavigation(activity: Activity, activeId: Int) {
        val navDiscovery = activity.findViewById<ImageView>(R.id.nav_discovery)
        val navChat = activity.findViewById<ImageView>(R.id.nav_chat)
        val navFriends = activity.findViewById<ImageView>(R.id.nav_friends)
        val navNotifications = activity.findViewById<ImageView>(R.id.nav_notifications)
        val navProfile = activity.findViewById<ImageView>(R.id.nav_profile)
        
        val dotChat = activity.findViewById<View>(R.id.dot_chat)
        val dotNotifications = activity.findViewById<View>(R.id.dot_notifications)

        val activeColor = ContextCompat.getColor(activity, R.color.primary)
        val inactiveColor = ContextCompat.getColor(activity, R.color.text_secondary)

        navDiscovery.setColorFilter(if (activeId == R.id.nav_discovery) activeColor else inactiveColor)
        navChat.setColorFilter(if (activeId == R.id.nav_chat) activeColor else inactiveColor)
        navFriends.setColorFilter(if (activeId == R.id.nav_friends) activeColor else inactiveColor)
        navNotifications.setColorFilter(if (activeId == R.id.nav_notifications) activeColor else inactiveColor)
        navProfile.setColorFilter(if (activeId == R.id.nav_profile) activeColor else inactiveColor)

        if (activity is AppCompatActivity) {
            val messageViewModel = ViewModelProvider(activity)[MessageViewModel::class.java]
            val friendViewModel = ViewModelProvider(activity)[FriendViewModel::class.java]

            activity.lifecycleScope.launch {
                activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        messageViewModel.chatRooms.collect { rooms ->
                            val currentUid = SupabaseClient.client.auth.currentUserOrNull()?.id
                            val hasUnread = rooms.any { room ->
                                room.lastMessage != null && 
                                room.lastMessage.senderId != currentUid && 
                                !room.lastMessage.isRead
                            }
                            dotChat?.visibility = if (hasUnread) View.VISIBLE else View.GONE
                        }
                    }
                    launch {
                        friendViewModel.incomingRequests.collect { requests ->
                            dotNotifications?.visibility = if (requests.isNotEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                }
            }
        }

        navDiscovery.setOnClickListener {
            if (activeId != R.id.nav_discovery) {
                activity.startActivity(Intent(activity, HomePage::class.java))
                activity.overridePendingTransition(0, 0)
                activity.finish()
            }
        }

        navChat.setOnClickListener {
            if (activeId != R.id.nav_chat) {
                activity.startActivity(Intent(activity, ChatActivity::class.java))
                activity.overridePendingTransition(0, 0)
                activity.finish()
            }
        }

        navNotifications.setOnClickListener {
            if (activeId != R.id.nav_notifications) {
                activity.startActivity(Intent(activity, FriendRequestActivity::class.java))
                activity.overridePendingTransition(0, 0)
                activity.finish()
            }
        }

        navProfile.setOnClickListener {
            if (activeId != R.id.nav_profile) {
                activity.startActivity(Intent(activity, ProfileActivity::class.java))
                activity.overridePendingTransition(0, 0)
                activity.finish()
            }
        }
    }
}