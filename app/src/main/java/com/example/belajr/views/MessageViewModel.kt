package com.example.belajr.views

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.belajr.controllers.AttachmentRepository
import com.example.belajr.controllers.FriendRepository
import com.example.belajr.controllers.MessageRepository
import com.example.belajr.models.ChatRoom
import com.example.belajr.models.Message
import com.example.belajr.models.PartnerResult
import com.example.belajr.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MessageViewModel : ViewModel() {

    private val repo = MessageRepository()
    private val friendRepo = FriendRepository()
    private var attachmentRepo: AttachmentRepository? = null

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms = _chatRooms.asStateFlow()

    private val _activeFriend = MutableStateFlow<PartnerResult?>(null)
    val activeFriend = _activeFriend.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var activeChannel: RealtimeChannel? = null
    private var chatJob: Job? = null
    private var profilesChannel: RealtimeChannel? = null
    private var roomsChannel: RealtimeChannel? = null

    private val currentUserId get() = SupabaseClient.client.auth.currentUserOrNull()?.id

    init {
        // Trigger initial loads and start listening
        loadChatRooms()
        listenToRooms()
        listenToProfiles()
    }

    fun openChat(otherUserId: String) {
        chatJob?.cancel()
        
        chatJob = viewModelScope.launch {
            activeChannel?.let { 
                it.unsubscribe()
                SupabaseClient.client.realtime.removeChannel(it)
            }
            activeChannel = null

            _isLoading.value = true
            
            viewModelScope.launch {
                repo.getFriendProfile(otherUserId).onSuccess {
                    _activeFriend.value = it
                }
            }

            repo.getMessages(otherUserId)
                .onSuccess { _messages.value = it }
                .onFailure { _error.value = it.message }

            try {
                val channelId = "messages-${otherUserId}-${System.currentTimeMillis()}"
                val channel = SupabaseClient.client.realtime.channel(channelId)
                activeChannel = channel

                channel.postgresChangeFlow<PostgresAction.Insert>(
                    schema = "public"
                ) {
                    table = "messages"
                }.onEach { change ->
                    try {
                        val newMessage = change.decodeRecord<Message>()
                        val isRelevant = newMessage.senderId == otherUserId || newMessage.receiverId == otherUserId
                        
                        if (isRelevant) {
                            val currentList = _messages.value
                            if (currentList.none { it.id == newMessage.id }) {
                                _messages.value = currentList.filterNot { 
                                    it.id != null && it.id > 1000000000000L && it.content == newMessage.content 
                                } + newMessage
                                
                                if (newMessage.senderId == otherUserId) {
                                    markAsRead(otherUserId)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MessageViewModel", "Gagal decode pesan: ${e.message}")
                    }
                }.launchIn(viewModelScope)

                channel.postgresChangeFlow<PostgresAction.Update>(
                    schema = "public"
                ) {
                    table = "messages"
                }.onEach { change ->
                    val updated = change.decodeRecord<Message>()
                    _messages.value = _messages.value.map { if (it.id == updated.id) updated else it }
                }.launchIn(viewModelScope)

                channel.subscribe()
                SupabaseClient.client.realtime.connect()
                
            } catch (e: Exception) {
                Log.e("MessageViewModel", "Realtime Error: ${e.message}")
            }

            _isLoading.value = false
        }
    }

    fun markAsRead(otherUserId: String) {
        viewModelScope.launch {
            repo.markMessagesAsRead(otherUserId).onSuccess {
                loadChatRooms()
            }
        }
    }

    fun sendMessage(receiverId: String, content: String) {
        val myId = currentUserId ?: return
        val now = getCurrentTimestamp()

        val tempId = System.currentTimeMillis()
        val tempMessage = Message(
            id = tempId,
            senderId = myId,
            receiverId = receiverId,
            content = content,
            sentAt = now,
            isRead = false
        )
        _messages.value = _messages.value + tempMessage

        viewModelScope.launch {
            repo.sendMessage(receiverId, content)
                .onFailure { 
                    _error.value = "Gagal mengirim: ${it.message}"
                    _messages.value = _messages.value.filter { it.id != tempId }
                }
        }
    }

    fun sendMessageWithImage(context: Context, receiverId: String, content: String?, uri: Uri) {
        val myId = currentUserId ?: return
        if (attachmentRepo == null) attachmentRepo = AttachmentRepository(context)
        
        val now = getCurrentTimestamp()
        val tempId = System.currentTimeMillis()

        val tempMessage = Message(
            id = tempId,
            senderId = myId,
            receiverId = receiverId,
            content = content,
            attachmentUrl = uri.toString(),
            sentAt = now,
            isRead = false
        )
        _messages.value = _messages.value + tempMessage

        viewModelScope.launch {
            _isLoading.value = true
            attachmentRepo!!.uploadFile(uri)
                .onSuccess { url ->
                    repo.sendMessageWithAttachment(receiverId, content, url)
                        .onFailure { 
                            _error.value = it.message
                            _messages.value = _messages.value.filter { it.id != tempId }
                        }
                }
                .onFailure { 
                    _error.value = "Gagal upload: ${it.message}"
                    _messages.value = _messages.value.filter { it.id != tempId }
                }
            _isLoading.value = false
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun loadChatRooms() {
        val myId = currentUserId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            friendRepo.getFriends()
                .onSuccess { friends ->
                    repo.getChatRooms(friends)
                        .onSuccess { rooms -> 
                            _chatRooms.value = rooms
                        }
                        .onFailure { _error.value = it.message }
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    private fun listenToRooms() {
        if (roomsChannel != null) return
        viewModelScope.launch {
            try {
                Log.d("MessageViewModel", "Connecting Realtime Rooms...")
                SupabaseClient.client.realtime.connect()
                val channelId = "global-rooms-${System.currentTimeMillis()}"
                val channel = SupabaseClient.client.realtime.channel(channelId)
                roomsChannel = channel
                
                channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") { table = "messages" }
                    .onEach { action ->
                        try {
                            val newMessage = action.decodeRecord<Message>()
                            if (newMessage.receiverId == currentUserId) {
                                Log.d("MessageViewModel", "New message detected for current user, refreshing chat list")
                                loadChatRooms() 
                            }
                        } catch (e: Exception) {
                            Log.e("MessageViewModel", "Error decoding global message: ${e.message}")
                            loadChatRooms() // fallback refresh
                        }
                    }.launchIn(viewModelScope)
                
                channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") { table = "messages" }
                    .onEach { 
                        loadChatRooms() 
                    }.launchIn(viewModelScope)
                
                channel.subscribe { status ->
                    Log.d("MessageViewModel", "Global Rooms Status: $status")
                }
            } catch (e: Exception) { Log.e("MessageViewModel", "Rooms Update Error: ${e.message}") }
        }
    }

    private fun listenToProfiles() {
        if (profilesChannel != null) return
        viewModelScope.launch {
            try {
                SupabaseClient.client.realtime.connect()
                val channelId = "global-profiles-${System.currentTimeMillis()}"
                val channel = SupabaseClient.client.realtime.channel(channelId)
                profilesChannel = channel
                channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") { table = "profiles" }
                    .onEach { change ->
                        val updatedProfile = change.decodeRecord<PartnerResult>()
                        if (_activeFriend.value?.id == updatedProfile.id) {
                            _activeFriend.value = updatedProfile
                        }
                        _chatRooms.value = _chatRooms.value.map { room ->
                            if (room.friend.id == updatedProfile.id) {
                                room.copy(friend = updatedProfile)
                            } else {
                                room
                            }
                        }
                    }.launchIn(viewModelScope)
                channel.subscribe()
            } catch (e: Exception) { Log.e("MessageViewModel", "Online Status Error: ${e.message}") }
        }
    }

    fun closeChat() {
        viewModelScope.launch {
            activeChannel?.let { 
                it.unsubscribe()
                SupabaseClient.client.realtime.removeChannel(it)
            }
            activeChannel = null
            _activeFriend.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        roomsChannel?.let { 
            SupabaseClient.client.realtime.removeChannel(it)
        }
        profilesChannel?.let {
            SupabaseClient.client.realtime.removeChannel(it)
        }
        closeChat()
    }
}