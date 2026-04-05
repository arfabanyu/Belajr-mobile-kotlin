package com.example.belajr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.belajr.models.RelationStatus
import com.example.belajr.views.MatchViewModel

class OtherProfileActivity : AppCompatActivity() {

    private lateinit var matchViewModel: MatchViewModel
    private var userId: String? = null
    private var username: String? = null
    private var relationStatus: RelationStatus = RelationStatus.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_other_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        matchViewModel = ViewModelProvider(this)[MatchViewModel::class.java]

        userId = intent.getStringExtra("USER_ID")
        username = intent.getStringExtra("USERNAME")
        val statusString = intent.getStringExtra("RELATION_STATUS")
        relationStatus = if (statusString != null) RelationStatus.valueOf(statusString) else RelationStatus.NONE

        setupViews()
    }

    private fun setupViews() {
        findViewById<TextView>(R.id.tvName).text = username ?: "User"
        findViewById<TextView>(R.id.tvInterests).text = intent.getStringExtra("INTERESTS") ?: "No interests listed"
        findViewById<TextView>(R.id.tvBio).text = intent.getStringExtra("BIO") ?: "No bio available"
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val btnAction = findViewById<Button>(R.id.btnMainAction)
        
        updateActionButton(btnAction)

        btnAction.setOnClickListener {
            handleAction(btnAction)
        }
    }

    private fun updateActionButton(btn: Button) {
        when (relationStatus) {
            RelationStatus.NONE -> {
                btn.text = "Send Friend Request"
                btn.isEnabled = true
            }
            RelationStatus.PENDING_OUT -> {
                btn.text = "Request Sent"
                btn.isEnabled = false
            }
            RelationStatus.PENDING_IN -> {
                btn.text = "Accept Request"
                btn.isEnabled = true
            }
            RelationStatus.FRIEND -> {
                btn.text = "Send Message"
                btn.isEnabled = true
            }
        }
    }

    private fun handleAction(btn: Button) {
        when (relationStatus) {
            RelationStatus.NONE -> {
                userId?.let {
                    matchViewModel.sendRequest(it, "")
                    relationStatus = RelationStatus.PENDING_OUT
                    updateActionButton(btn)
                    Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
                }
            }
            RelationStatus.FRIEND -> {
                val intent = Intent(this, ChatDetailActivity::class.java).apply {
                    putExtra("RECEIVER_ID", userId)
                    putExtra("RECEIVER_NAME", username)
                }
                startActivity(intent)
            }
            RelationStatus.PENDING_IN -> {
                // Logic untuk accept bisa ditambahkan di sini via FriendViewModel
                Toast.makeText(this, "Please accept via Notifications", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
}