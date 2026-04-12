package com.example.belajr

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.belajr.controllers.AuthRepository
import com.example.belajr.models.ProfileUpdate
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BelajrApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val authRepo = AuthRepository()

    override fun onCreate() {
        super.onCreate()
        
        // Global Realtime Connection
        applicationScope.launch {
            try {
                SupabaseClient.client.realtime.connect()
                Log.d("BelajrApp", "Global Realtime Connected")
            } catch (e: Exception) {
                Log.e("BelajrApp", "Global Realtime Error: ${e.message}")
            }
        }
        
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                updateStatus(true)
                applicationScope.launch { 
                    try {
                        SupabaseClient.client.realtime.connect()
                    } catch (e: Exception) { /* Silent */ }
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                updateStatus(false)
            }
        })
    }

    private fun updateStatus(isOnline: Boolean) {
        if (authRepo.isLoggedIn()) {
            applicationScope.launch {
                authRepo.updateProfile(ProfileUpdate(isOnline = isOnline))
            }
        }
    }
}