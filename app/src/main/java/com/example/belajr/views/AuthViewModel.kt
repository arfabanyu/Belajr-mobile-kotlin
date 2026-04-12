package com.example.belajr.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.belajr.SupabaseClient
import com.example.belajr.controllers.AuthRepository
import com.example.belajr.controllers.NotificationRepository
import com.example.belajr.models.Profile
import com.example.belajr.models.ProfileUpdate
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object LoggedOut : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile = _profile.asStateFlow()

    init {
        observeSessionStatus()
    }

    private fun observeSessionStatus() {
        viewModelScope.launch {
            SupabaseClient.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        updateOnlineStatus(true)
                        if (_authState.value !is AuthState.Success) {
                            _authState.value = AuthState.Success
                            loadProfile()
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        if (_authState.value is AuthState.Success) {
                            _authState.value = AuthState.LoggedOut
                            _profile.value = null
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            repo.updateProfile(ProfileUpdate(isOnline = isOnline))
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.register(email, password, username)
                .onSuccess { 
                    _authState.value = AuthState.Success 
                }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: "Register gagal"
                    )
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.login(email, password)
                .onSuccess {
                    NotificationRepository().registerFcmToken()
                }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: "Login gagal"
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            updateOnlineStatus(false)
            NotificationRepository().clearFcmToken()
            repo.logout()
                .onSuccess {
                    // pakai SessionStatus.NotAuthenticated
                }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: "Logout gagal"
                    )
                }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            repo.getCurrentProfile()
                .onSuccess { _profile.value = it }
        }
    }

    fun updateProfile(data: ProfileUpdate) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.updateProfile(data)
                .onSuccess {
                    _authState.value = AuthState.Success
                    loadProfile()
                }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: "Gagal update profil"
                    )
                }
        }
    }

    suspend fun uploadAvatar(byteArray: ByteArray, fileName: String): Result<String> {
        return repo.uploadAvatar(byteArray, fileName)
    }

    fun deleteOldAvatar(fileName: String) {
        viewModelScope.launch {
            repo.deleteAvatar(fileName)
        }
    }
}
