package com.example.belajr

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.adapters.ChatAdapter
import com.example.belajr.views.MessageViewModel
import kotlinx.coroutines.launch

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: MessageViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    
    private var receiverId: String? = null
    private var receiverName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_detail)

        receiverId = intent.getStringExtra("RECEIVER_ID")
        receiverName = intent.getStringExtra("RECEIVER_NAME")

        if (receiverId == null) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]
        
        setupViews()
        setupRecyclerView()
        observeMessages()

        viewModel.openChat(receiverId!!)
    }

    private fun setupViews() {
        findViewById<TextView>(R.id.tvUserName).text = receiverName ?: "Chat"
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        btnSend.setOnClickListener {
            val content = etMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.sendMessage(receiverId!!, content)
                etMessage.text.clear()
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(emptyList())
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Mulai dari bawah seperti WhatsApp
        }
        rvMessages.adapter = chatAdapter
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { messages ->
                    chatAdapter.updateMessages(messages)
                    if (messages.isNotEmpty()) {
                        rvMessages.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (error != null) {
                    Toast.makeText(this@ChatDetailActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.closeChat()
    }
}