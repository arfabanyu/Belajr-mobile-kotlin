package com.example.belajr.controllers

import com.example.belajr.models.ProfileUpdate
import com.example.belajr.SupabaseClient
import com.example.belajr.models.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthRepository {

    // Register
    suspend fun register(
        email: String,
        password: String,
        username: String
    ): Result<Unit> = runCatching {
        // 1. Buat akun di Supabase Auth
        SupabaseClient.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        // 2. Update username di profiles
        // (trigger sudah auto-insert row-nya,
        // tinggal update kolom username)
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Register gagal")

        SupabaseClient.client.postgrest["profiles"]
            .update({ set("username", username) }) {
                filter { eq("id", userId) }
            }
    }

    // Login
    suspend fun login(
        email: String,
        password: String
    ): Result<Unit> = runCatching {
        SupabaseClient.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    // Logout
    suspend fun logout(): Result<Unit> = runCatching {
        SupabaseClient.client.auth.signOut()
    }

    // Ambil profil user yang sedang login
    suspend fun getCurrentProfile(): Result<Profile> = runCatching {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Belum login")

        SupabaseClient.client.postgrest["profiles"]
            .select { filter { eq("id", userId) } }
            .decodeSingle<Profile>()
    }

    // Update profil
    suspend fun updateProfile(
        data: ProfileUpdate
    ): Result<Unit> = runCatching {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Belum login")

        SupabaseClient.client.postgrest["profiles"]
            .update(data) { filter { eq("id", userId) } }
    }

    // Cek apakah user sudah login
    // (untuk splash screen / auto-login)
    fun isLoggedIn(): Boolean {
        return SupabaseClient.client.auth.currentUserOrNull() != null
    }
}