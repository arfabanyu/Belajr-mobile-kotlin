package com.example.belajr

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.belajr.adapters.InboxAdapter
import com.example.belajr.adapters.OnlineFriendAdapter
import com.example.belajr.models.ChatRoom
import com.example.belajr.models.RelationStatus
import com.example.belajr.views.AuthViewModel
import com.example.belajr.views.MessageViewModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: MessageViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var inboxAdapter: InboxAdapter
    private lateinit var onlineAdapter: OnlineFriendAdapter
    
    private lateinit var rvChatList: RecyclerView
    private lateinit var rvOnlineFriends: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var llIndicators: LinearLayout
    private lateinit var layoutEmptyChats: LinearLayout
    private lateinit var ivProfile: ImageView
    
    private var allChatRooms: List<ChatRoom> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        layoutEmptyChats = findViewById(R.id.layoutEmptyChats)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupViews()
        setupRecyclerViews()
        NavigationUtils.setupBottomNavigation(this, R.id.nav_chat)
        observeData()
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadChatRooms()
        authViewModel.loadProfile()
    }

    private fun setupViews() {
        rvChatList = findViewById(R.id.rvChatList)
        rvOnlineFriends = findViewById(R.id.rvOnlineFriends)
        etSearch = findViewById(R.id.etSearch)
        llIndicators = findViewById(R.id.llIndicators)
        ivProfile = findViewById(R.id.ivProfile)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterChats(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        ivProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun filterChats(query: String) {
        val filtered = if (query.isEmpty()) {
            allChatRooms
        } else {
            allChatRooms.filter { 
                it.friend.username.contains(query, ignoreCase = true) 
            }
        }
        inboxAdapter.updateData(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            layoutEmptyChats.visibility = View.VISIBLE
            rvChatList.visibility = View.GONE
        } else {
            layoutEmptyChats.visibility = View.GONE
            rvChatList.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerViews() {
        inboxAdapter = InboxAdapter(emptyList()) { room ->
            val intent = Intent(this, ChatDetailActivity::class.java).apply {
                putExtra("RECEIVER_ID", room.friend.id)
                putExtra("RECEIVER_NAME", room.friend.username)
            }
            startActivity(intent)
        }
        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.adapter = inboxAdapter

        onlineAdapter = OnlineFriendAdapter(emptyList()) { friend ->
            val intent = Intent(this, OtherProfileActivity::class.java).apply {
                putExtra("USER_ID", friend.id)
                putExtra("USERNAME", friend.username)
                putExtra("BIO", friend.learningStatus)
                putExtra("INTERESTS", friend.interests?.joinToString(", "))
                putExtra("RELATION_STATUS", RelationStatus.FRIEND.name)
            }
            startActivity(intent)
        }
        rvOnlineFriends.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvOnlineFriends.adapter = onlineAdapter
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatRooms.collect { rooms ->
                    allChatRooms = rooms
                    inboxAdapter.updateData(rooms)
                    updateEmptyState(rooms.isEmpty())
                    
                    val onlineFriends = rooms.map { it.friend }
                    onlineAdapter.updateData(onlineFriends)
                    
                    updateIndicators(onlineFriends.size)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.profile.collect { profile ->
                    if (profile != null) {
                        Glide.with(this@ChatActivity)
                            .load(profile.avatarUrl)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .circleCrop()
                            .into(ivProfile)
                    }
                }
            }
        }
    }

    private fun updateIndicators(count: Int) {
        llIndicators.removeAllViews()
        if (count <= 1) return

        for (i in 0 until count) {
            val dot = View(this).apply {
                val size = (6 * resources.displayMetrics.density).toInt()
                val margin = (2 * resources.displayMetrics.density).toInt()
                val params = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, margin, margin, margin)
                }
                layoutParams = params
                background = if (i == 0) {
                    getDrawable(R.drawable.bg_indicator_active)
                } else {
                    getDrawable(R.drawable.bg_indicator_inactive)
                }
            }
            llIndicators.addView(dot)
        }
    }
}
