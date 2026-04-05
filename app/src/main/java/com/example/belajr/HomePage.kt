package com.example.belajr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.adapters.PartnerAdapter
import com.example.belajr.models.RelationStatus
import com.example.belajr.views.MatchViewModel
import kotlinx.coroutines.launch

class HomePage : AppCompatActivity() {

    private lateinit var matchViewModel: MatchViewModel
    private lateinit var rvPartners: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: PartnerAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        matchViewModel = ViewModelProvider(this)[MatchViewModel::class.java]

        rvPartners = findViewById(R.id.rvPartners)
        etSearch = findViewById(R.id.etSearch)

        setupRecyclerView()
        setupSearch()
        setupNavigation()
        observePartners()
        askNotificationPermission()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = PartnerAdapter(emptyList()) { item ->
            when (item.relationStatus) {
                RelationStatus.NONE -> {
                    matchViewModel.sendRequest(item.profile.id, etSearch.text.toString())
                }
                RelationStatus.FRIEND -> {
                    val intent = Intent(this, ChatDetailActivity::class.java).apply {
                        putExtra("RECEIVER_ID", item.profile.id)
                        putExtra("RECEIVER_NAME", item.profile.username)
                    }
                    startActivity(intent)
                }
                else -> {}
            }
        }
        rvPartners.layoutManager = LinearLayoutManager(this)
        rvPartners.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                matchViewModel.search(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.nav_chat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        findViewById<ImageView>(R.id.nav_notifications).setOnClickListener {
            startActivity(Intent(this, FriendRequestActivity::class.java))
        }
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            // startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun observePartners() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                matchViewModel.results.collect { partners ->
                    adapter.updateData(partners)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                matchViewModel.error.collect { error ->
                    if (error != null) {
                        Toast.makeText(this@HomePage, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}