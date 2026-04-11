package com.example.belajr.controllers

import com.example.belajr.models.FriendRequest
import com.example.belajr.models.Friendship
import com.example.belajr.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

class FriendRepository {

    private val currentUserId get() =
    SupabaseClient.client.auth.currentUserOrNull()?.id ?: error("Not logged in")

    suspend fun sendRequest(receiverId: String): Result<Unit> = runCatching {
        SupabaseClient.client.postgrest["friend_requests"].insert(
            FriendRequest(
                senderId = currentUserId,
                receiverId = receiverId,
                status = "pending"
            )
        )
    }

    suspend fun cancelRequest(receiverId: String, requestId: Long? = null): Result<Unit> = runCatching {
        // Supabase Postgrest delete membutuhkan filter yang tepat. 
        // Pastikan nama kolom di database sesuai (sender_id, receiver_id)
        SupabaseClient.client.postgrest["friend_requests"].delete {
            filter {
                if (requestId != null) {
                    // Hapus berdasarkan ID unik (Paling Ampuh)
                    eq("id", requestId)
                } else {
                    // Fallback jika ID tidak ada
                    eq("sender_id", currentUserId)
                    eq("receiver_id", receiverId)
                    eq("status", "pending")
                }
            }
        }
    }

    suspend fun getIncomingRequests(): Result<List<FriendRequest>> = runCatching {
        SupabaseClient.client.postgrest["friend_requests"]
            .select {
                filter {
                    eq("receiver_id", currentUserId)
                    eq("status", "pending")
                }
            }
            .decodeList<FriendRequest>()
    }

    suspend fun getOutgoingRequests(): Result<List<FriendRequest>> = runCatching {
        SupabaseClient.client.postgrest["friend_requests"]
            .select {
                filter {
                    eq("sender_id", currentUserId)
                    eq("status", "pending")
                }
            }
            .decodeList<FriendRequest>()
    }

    suspend fun acceptRequest(requestId: Long, senderId: String): Result<Unit> = runCatching {
        SupabaseClient.client.postgrest["friend_requests"]
            .update({ 
                set("status", "accepted") 
            }) {
                filter { eq("id", requestId) }
            }

        // Insert ke tabel friendships
        SupabaseClient.client.postgrest["friendships"].insert(
            Friendship(
                userOneId = senderId,
                userTwoId = currentUserId
            )
        )
    }

    suspend fun rejectRequest(requestId: Long): Result<Unit> = runCatching {
        SupabaseClient.client.postgrest["friend_requests"]
            .update({ 
                set("status", "rejected") 
            }) {
                filter { eq("id", requestId) }
            }
    }

    suspend fun getFriends(): Result<List<Friendship>> = runCatching {
        SupabaseClient.client.postgrest["friendships"]
            .select {
                filter {
                    or {
                        eq("user_one_id", currentUserId)
                        eq("user_two_id", currentUserId)
                    }
                }
            }
            .decodeList<Friendship>()
    }

    suspend fun getFriendCount(userId: String): Result<Int> = runCatching {
        val result = SupabaseClient.client.postgrest["friendships"]
            .select {
                filter {
                    or {
                        eq("user_one_id", userId)
                        eq("user_two_id", userId)
                    }
                }
            }
            .decodeList<Friendship>()
        result.size
    }

    suspend fun isFriend(otherUserId: String): Boolean {
        return try {
            val result = SupabaseClient.client.postgrest["friendships"]
                .select {
                    filter {
                        or {
                            and {
                                eq("user_one_id", currentUserId)
                                eq("user_two_id", otherUserId)
                            }
                            and {
                                eq("user_one_id", otherUserId)
                                eq("user_two_id", currentUserId)
                            }
                        }
                    }
                }
                .decodeList<Friendship>()
            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}