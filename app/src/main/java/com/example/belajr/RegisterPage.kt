package com.example.belajr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
import com.example.belajr.views.AuthState
import com.example.belajr.views.AuthViewModel
import kotlinx.coroutines.launch

class RegisterPage : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val usernameEditText = findViewById<EditText>(R.id.username)
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val registerButton = findViewById<Button>(R.id.mainButton)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        tvLogin.setOnClickListener {
            finish() // Kembali ke LoginPage
        }

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.register(email, password, username)
            } else {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            }
        }

        observeAuthState()
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            findViewById<Button>(R.id.mainButton).isEnabled = false
                            findViewById<Button>(R.id.mainButton).text = "Creating Account..."
                        }
                        is AuthState.Success -> {
                            Toast.makeText(this@RegisterPage, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_SHORT).show()
                            finish() // Kembali ke login
                        }
                        is AuthState.Error -> {
                            findViewById<Button>(R.id.mainButton).isEnabled = true
                            findViewById<Button>(R.id.mainButton).text = "Create Account"
                            Toast.makeText(this@RegisterPage, state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            findViewById<Button>(R.id.mainButton).isEnabled = true
                            findViewById<Button>(R.id.mainButton).text = "Create Account"
                        }
                    }
                }
            }
        }
    }
}