package com.example.belajr

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.belajr.models.RelationStatus
import com.example.belajr.views.MatchViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class OtherProfileActivity : AppCompatActivity() {

    private lateinit var matchViewModel: MatchViewModel
    private var userId: String? = null
    private var username: String? = null
    private var relationStatus: RelationStatus = RelationStatus.NONE
    private var requestId: Long? = null

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
        requestId = intent.getLongExtra("REQUEST_ID", -1).takeIf { it != -1L }
        
        val statusString = intent.getStringExtra("RELATION_STATUS")
        relationStatus = if (statusString != null) RelationStatus.valueOf(statusString) else RelationStatus.NONE

        setupViews()
        observeViewModel()
        
        userId?.let { matchViewModel.loadFriendCount(it) }
    }

    private fun setupViews() {
        findViewById<TextView>(R.id.tvName).text = username ?: "User"
        
        val bioFromDb = intent.getStringExtra("BIO") ?: "No bio available"
        findViewById<TextView>(R.id.tvAboutDetail).text = bioFromDb
        
        val interestsString = intent.getStringExtra("INTERESTS") ?: ""
        val interestsList = if (interestsString.isNotEmpty()) interestsString.split(", ") else emptyList()
        
        setupInterestsChips(interestsList)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val btnAction = findViewById<Button>(R.id.btnMainAction)
        val btnMessage = findViewById<Button>(R.id.btnMessage)
        
        updateActionButton(btnAction, btnMessage)

        btnAction.setOnClickListener {
            handleAction(btnAction, btnMessage)
        }

        btnMessage.setOnClickListener {
            val intent = Intent(this, ChatDetailActivity::class.java).apply {
                putExtra("RECEIVER_ID", userId)
                putExtra("RECEIVER_NAME", username)
            }
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                matchViewModel.friendCount.collect { count ->
                    findViewById<TextView>(R.id.tvFriendCount).text = count.toString()
                }
            }
        }
    }

    private fun setupInterestsChips(interests: List<String>) {
        val chipGroup = findViewById<ChipGroup>(R.id.cgInterests)
        chipGroup.removeAllViews()
        
        for (interest in interests) {
            val chip = Chip(this)
            chip.text = interest
            chip.isClickable = false
            chip.isCheckable = false
            chip.setChipBackgroundColorResource(R.color.bg_light)
            chip.setTextColor(getColor(R.color.primary))
            chip.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2)
            chipGroup.addView(chip)
        }
    }

    private fun updateActionButton(btn: Button, btnMessage: Button) {
        val mBtnAction = btn as? MaterialButton

        if (relationStatus == RelationStatus.FRIEND) {
            btn.visibility = View.GONE
            btnMessage.visibility = View.VISIBLE
            btnMessage.layoutParams = (btnMessage.layoutParams as android.widget.LinearLayout.LayoutParams).apply {
                weight = 2.2f
                marginStart = 0
            }
        } else {
            btn.visibility = View.VISIBLE
            btnMessage.visibility = View.GONE
            
            when (relationStatus) {
                RelationStatus.NONE -> {
                    btn.text = "Send Request"
                    btn.isEnabled = true
                    btn.alpha = 1.0f
                    // Reset ke warna primary
                    mBtnAction?.backgroundTintList = ColorStateList.valueOf(getColor(R.color.primary))
                    btn.setTextColor(Color.WHITE)
                    mBtnAction?.strokeWidth = 0
                }
                RelationStatus.PENDING_OUT -> {
                    btn.text = "Cancel Request"
                    btn.isEnabled = true
                    btn.alpha = 1.0f
                    
                    val redColor = Color.parseColor("#FF5252")
                    // Menggunakan background putih dan outline merah
                    mBtnAction?.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                    btn.setTextColor(redColor)
                    
                    if (mBtnAction != null) {
                        mBtnAction.strokeColor = ColorStateList.valueOf(redColor)
                        mBtnAction.strokeWidth = 4
                        mBtnAction.rippleColor = ColorStateList.valueOf(redColor.withAlpha(20))
                    }
                }
                RelationStatus.PENDING_IN -> {
                    btn.text = "Accept Request"
                    btn.isEnabled = true
                    btn.alpha = 1.0f
                    mBtnAction?.backgroundTintList = ColorStateList.valueOf(getColor(R.color.primary))
                    btn.setTextColor(Color.WHITE)
                    mBtnAction?.strokeWidth = 0
                }
                else -> {}
            }
        }
    }

    private fun handleAction(btn: Button, btnMessage: Button) {
        when (relationStatus) {
            RelationStatus.NONE -> {
                userId?.let {
                    matchViewModel.sendRequest(it, "")
                    relationStatus = RelationStatus.PENDING_OUT
                    updateActionButton(btn, btnMessage)
                    Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
                }
            }
            RelationStatus.PENDING_OUT -> {
                userId?.let {
                    matchViewModel.cancelRequest(it, requestId, "")
                    relationStatus = RelationStatus.NONE
                    updateActionButton(btn, btnMessage)
                    Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show()
                }
            }
            RelationStatus.PENDING_IN -> {
                Toast.makeText(this, "Please accept via Notifications", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    // Helper extension to add alpha to Int color
    private fun Int.withAlpha(alpha: Int): Int {
        return (alpha shl 24) or (this and 0x00FFFFFF)
    }
}
