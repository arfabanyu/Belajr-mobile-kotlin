package com.example.belajr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.adapters.InboxAdapter
import com.example.belajr.models.ChatRoom
import com.example.belajr.views.MessageViewModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: MessageViewModel
    private lateinit var inboxAdapter: InboxAdapter
    private lateinit var rvChatList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]

        setupRecyclerView()
        setupNavigation()
        observeInbox()

        viewModel.loadChatRooms()
    }

    private fun setupRecyclerView() {
        rvChatList = findViewById(R.id.rvChatList)
        inboxAdapter = InboxAdapter(emptyList<ChatRoom>()) { room ->
            val intent = Intent(this, ChatDetailActivity::class.java).apply {
                putExtra("RECEIVER_ID", room.friend.id)
                putExtra("RECEIVER_NAME", room.friend.username)
            }
            startActivity(intent)
        }
        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.adapter = inboxAdapter
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.nav_discovery).setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
            finish()
        }
        // nav_chat is current page
        
        findViewById<ImageView>(R.id.nav_notifications).setOnClickListener {
            startActivity(Intent(this, FriendRequestActivity::class.java))
            finish()
        }
        
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    private fun observeInbox() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatRooms.collect { rooms ->
                    inboxAdapter.updateData(rooms)
                }
            }
        }
    }
}