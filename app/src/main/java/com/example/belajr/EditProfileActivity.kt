package com.example.belajr

import android.os.Bundle
import android.widget.EditText
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
import com.example.belajr.models.ProfileUpdate
import com.example.belajr.views.AuthState
import com.example.belajr.views.AuthViewModel
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var etUsername: EditText
    private lateinit var etBio: EditText
    private lateinit var btnSave: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupViews()
        observeProfile()

        authViewModel.loadProfile()
    }

    private fun setupViews() {
        etUsername = findViewById(R.id.etUsername)
        etBio = findViewById(R.id.etBio)
        btnSave = findViewById(R.id.btnSave)
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val bio = etBio.text.toString().trim()

            if (username.isNotEmpty()) {
                authViewModel.updateProfile(ProfileUpdate(username = username, learningStatus = bio))
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeProfile() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.profile.collect { profile ->
                    if (profile != null) {
                        etUsername.setText(profile.username)
                        etBio.setText(profile.learningStatus ?: "") 
                        findViewById<TextView>(R.id.etEmail).setText(profile.email)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> btnSave.isEnabled = false
                        is AuthState.Success -> {
                            btnSave.isEnabled = true
                            Toast.makeText(this@EditProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is AuthState.Error -> {
                            btnSave.isEnabled = true
                            Toast.makeText(this@EditProfileActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> btnSave.isEnabled = true
                    }
                }
            }
        }
    }
}