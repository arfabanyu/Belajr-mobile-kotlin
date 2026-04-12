package com.example.belajr.views

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.belajr.SupabaseClient
import com.example.belajr.controllers.FriendRepository
import com.example.belajr.models.FriendRequest
import com.example.belajr.models.Friendship
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FriendViewModel : ViewModel() {

    private val repo = FriendRepository()

    private val _incomingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incomingRequests = _incomingRequests.asStateFlow()

    private val _friends = MutableStateFlow<List<Friendship>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var requestsChannel: RealtimeChannel? = null
    
    private val currentUserId get() = SupabaseClient.client.auth.currentUserOrNull()?.id

    init {
        loadIncomingRequests()
        listenToRequests()
    }

    private fun listenToRequests() {
        if (requestsChannel != null) return
        viewModelScope.launch {
            try {
                SupabaseClient.client.realtime.connect()
                val channelName = "friend-req-${System.currentTimeMillis()}"
                val channel = SupabaseClient.client.realtime.channel(channelName)
                requestsChannel = channel
                
                channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") { 
                    table = "friend_requests" 
                }.onEach { action -> 
                    try {
                        val req = action.decodeRecord<FriendRequest>()
                        if (req.receiverId == currentUserId) {
                            Log.d("FriendViewModel", "New friend request for me!")
                            loadIncomingRequests() 
                        }
                    } catch (e: Exception) {
                        loadIncomingRequests()
                    }
                }.launchIn(viewModelScope)

                channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") { 
                    table = "friend_requests" 
                }.onEach { loadIncomingRequests() }.launchIn(viewModelScope)

                channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") { 
                    table = "friend_requests" 
                }.onEach { loadIncomingRequests() }.launchIn(viewModelScope)
                
                channel.subscribe { status ->
                    Log.d("FriendViewModel", "Requests Channel Status: $status")
                }
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Realtime Requests Error: ${e.message}")
            }
        }
    }

    fun loadIncomingRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getIncomingRequests()
                .onSuccess { _incomingRequests.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            repo.getFriends()
                .onSuccess { _friends.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun sendRequest(receiverId: String) {
        viewModelScope.launch {
            repo.sendRequest(receiverId)
                .onFailure { _error.value = it.message }
        }
    }

    fun acceptRequest(requestId: Long, senderId: String) {
        viewModelScope.launch {
            repo.acceptRequest(requestId, senderId)
                .onSuccess { loadIncomingRequests() }
                .onFailure { _error.value = it.message }
        }
    }

    fun rejectRequest(requestId: Long) {
        viewModelScope.launch {
            repo.rejectRequest(requestId)
                .onSuccess { loadIncomingRequests() }
                .onFailure { _error.value = it.message }
        }
    }

    override fun onCleared() {
        super.onCleared()
        requestsChannel?.let {
            val channel = it
            viewModelScope.launch {
                try {
                    SupabaseClient.client.realtime.removeChannel(channel)
                } catch (e: Exception) {
                    Log.e("FriendViewModel", "Error removing channel: ${e.message}")
                }
            }
        }
        requestsChannel = null
    }
}