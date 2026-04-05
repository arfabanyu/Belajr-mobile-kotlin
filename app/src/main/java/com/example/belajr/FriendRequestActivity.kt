package com.example.belajr

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.adapters.FriendRequestAdapter
import com.example.belajr.views.FriendViewModel
import kotlinx.coroutines.launch

class FriendRequestActivity : AppCompatActivity() {

    private lateinit var viewModel: FriendViewModel
    private lateinit var adapter: FriendRequestAdapter
    private lateinit var rvRequests: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_request)

        viewModel = ViewModelProvider(this)[FriendViewModel::class.java]

        setupRecyclerView()
        setupViews()
        observeRequests()

        viewModel.loadIncomingRequests()
    }

    private fun setupViews() {
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        rvRequests = findViewById(R.id.rvFriendRequests)
        adapter = FriendRequestAdapter(
            emptyList(),
            onAccept = { request ->
                viewModel.acceptRequest(request.id!!, request.senderId)
                Toast.makeText(this, "Friend request accepted!", Toast.LENGTH_SHORT).show()
            },
            onReject = { request ->
                viewModel.rejectRequest(request.id!!)
                Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show()
            }
        )
        rvRequests.layoutManager = LinearLayoutManager(this)
        rvRequests.adapter = adapter
    }

    private fun observeRequests() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.incomingRequests.collect { requests ->
                    adapter.updateData(requests)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (error != null) {
                    Toast.makeText(this@FriendRequestActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}