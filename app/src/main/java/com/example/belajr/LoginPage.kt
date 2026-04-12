package com.example.belajr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.belajr.views.AuthState
import com.example.belajr.views.AuthViewModel
import kotlinx.coroutines.launch

class LoginPage : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen sebelum super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.mainButton)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterPage::class.java))
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.login(email, password)
            } else {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        observeAuthState()
        
        // Cek apakah user sudah login sebelumnya
        authViewModel.checkSession()
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            findViewById<Button>(R.id.mainButton).isEnabled = false
                            findViewById<Button>(R.id.mainButton).text = "Loading..."
                        }
                        is AuthState.Success -> {
                            // Cek apakah context masih valid sebelum pindah halaman
                            if (!isFinishing) {
                                startActivity(Intent(this@LoginPage, HomePage::class.java))
                                finish()
                            }
                        }
                        is AuthState.Error -> {
                            findViewById<Button>(R.id.mainButton).isEnabled = true
                            findViewById<Button>(R.id.mainButton).text = "Login"
                            Toast.makeText(this@LoginPage, state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            findViewById<Button>(R.id.mainButton).isEnabled = true
                            findViewById<Button>(R.id.mainButton).text = "Login"
                        }
                    }
                }
            }
        }
    }
}