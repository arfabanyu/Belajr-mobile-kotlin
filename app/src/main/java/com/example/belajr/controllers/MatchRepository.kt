package com.example.belajr.controllers

import com.example.belajr.SupabaseClient
import com.example.belajr.models.FriendRequest
import com.example.belajr.models.Friendship
import com.example.belajr.models.PartnerResult
import com.example.belajr.models.PartnerWithStatus
import com.example.belajr.models.RelationStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth

class MatchRepository {

    private val currentUserId get() =
        SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Belum login")

    suspend fun getAllAvailableInterests(): Result<List<String>> = runCatching {
        val profiles = SupabaseClient.client.postgrest["profiles"]
            .select()
            .decodeList<PartnerResult>()
        
        profiles.flatMap { it.interests ?: emptyList() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    suspend fun searchPartners(keyword: String): Result<List<PartnerWithStatus>> =
        runCatching {
            val profiles = SupabaseClient.client.postgrest["profiles"]
                .select {
                    filter {
                        neq("id", currentUserId)
                        if (keyword.isNotEmpty()) {
                            contains("interests", listOf(keyword))
                        }
                    }
                }
                .decodeList<PartnerResult>()

            val requests = SupabaseClient.client.postgrest["friend_requests"]
                .select {
                    filter {
                        eq("status", "pending")
                        or {
                            eq("sender_id", currentUserId)
                            eq("receiver_id", currentUserId)
                        }
                    }
                }
                .decodeList<FriendRequest>()

            val friendships = SupabaseClient.client.postgrest["friendships"]
                .select {
                    filter {
                        or {
                            eq("user_one_id", currentUserId)
                            eq("user_two_id", currentUserId)
                        }
                    }
                }
                .decodeList<Friendship>()

            profiles.map { profile ->
                // Cari request yang melibatkan user ini
                val request = requests.firstOrNull { 
                    it.senderId == profile.id || it.receiverId == profile.id 
                }
                
                val status = when {
                    friendships.any {
                        (it.userOneId == profile.id && it.userTwoId == currentUserId) ||
                        (it.userOneId == currentUserId && it.userTwoId == profile.id)
                    } -> RelationStatus.FRIEND
                    request == null -> RelationStatus.NONE
                    request.senderId == currentUserId -> RelationStatus.PENDING_OUT
                    else -> RelationStatus.PENDING_IN
                }
                
                PartnerWithStatus(profile, status, request?.id)
            }
        }
}