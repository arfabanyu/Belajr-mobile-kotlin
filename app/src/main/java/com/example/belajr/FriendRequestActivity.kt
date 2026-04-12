package com.example.belajr

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
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
import com.example.belajr.adapters.FriendRequestAdapter
import com.example.belajr.views.AuthViewModel
import com.example.belajr.views.FriendViewModel
import kotlinx.coroutines.launch

class FriendRequestActivity : AppCompatActivity() {

    private lateinit var friendViewModel: FriendViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var adapter: FriendRequestAdapter
    private lateinit var rvRequests: RecyclerView
    private lateinit var layoutEmpty: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_request)

        layoutEmpty = findViewById(R.id.layoutEmpty)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        friendViewModel = ViewModelProvider(this)[FriendViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupRecyclerView()
        setupProfileHeader()
        
        NavigationUtils.setupBottomNavigation(this, R.id.nav_notifications)

        observeRequests()

        friendViewModel.loadIncomingRequests()
        authViewModel.loadProfile()
    }

    private fun setupProfileHeader() {
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        lifecycleScope.launch {
            authViewModel.profile.collect { profile ->
                if (profile?.avatarUrl != null) {
                    Glide.with(this@FriendRequestActivity)
                        .load(profile.avatarUrl)
                        .placeholder(R.drawable.default_profile)
                        .circleCrop()
                        .into(ivProfile)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        rvRequests = findViewById(R.id.rvFriendRequests)
        adapter = FriendRequestAdapter(
            emptyList(),
            onAccept = { request ->
                friendViewModel.acceptRequest(request.id!!, request.senderId)
                Toast.makeText(this, "Friend request accepted!", Toast.LENGTH_SHORT).show()
            },
            onReject = { request ->
                friendViewModel.rejectRequest(request.id!!)
                Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show()
            }
        )
        rvRequests.layoutManager = LinearLayoutManager(this)
        rvRequests.adapter = adapter
    }

    private fun observeRequests() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                friendViewModel.incomingRequests.collect { requests ->
                    adapter.updateData(requests)
                    if (requests.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                        rvRequests.visibility = View.GONE
                    } else {
                        layoutEmpty.visibility = View.GONE
                        rvRequests.visibility = View.VISIBLE
                    }
                }
            }
        }

        lifecycleScope.launch {
            friendViewModel.error.collect { error ->
                if (error != null) {
                    Toast.makeText(this@FriendRequestActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}