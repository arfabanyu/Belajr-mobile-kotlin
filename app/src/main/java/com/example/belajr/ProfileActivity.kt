package com.example.belajr

import android.content.Intent
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.example.belajr.views.AuthState
import com.example.belajr.views.AuthViewModel
import com.example.belajr.views.MatchViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var matchViewModel: MatchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        matchViewModel = ViewModelProvider(this)[MatchViewModel::class.java]
        
        NavigationUtils.setupBottomNavigation(this, R.id.nav_profile)

        setupViews()
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        authViewModel.loadProfile()
    }

    private fun setupViews() {
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            authViewModel.logout()
        }

        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.LoggedOut -> {
                            val intent = Intent(this@ProfileActivity, LoginPage::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        is AuthState.Error -> {
                            Toast.makeText(this@ProfileActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.profile.collect { profile ->
                    if (profile != null) {
                        findViewById<TextView>(R.id.tvName).text = profile.username
                        findViewById<TextView>(R.id.tvAboutDetail).text = profile.learningStatus ?: "Belum ada bio."
                        
                        val imgAvatar = findViewById<ImageView>(R.id.imgAvatar)
                        Glide.with(this@ProfileActivity)
                            .load(profile.avatarUrl)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .circleCrop()
                            .into(imgAvatar)
                        
                        setupInterestsChips(profile.interests ?: emptyList())
                        
                        matchViewModel.loadFriendCount(profile.id)
                    }
                }
            }
        }

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
}
