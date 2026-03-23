package com.example.belajr.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.belajr.controllers.FriendRepository
import com.example.belajr.models.FriendRequest
import com.example.belajr.models.Friendship
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
}