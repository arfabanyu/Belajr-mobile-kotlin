package com.example.belajr

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.SettingsSessionManager
import java.util.concurrent.TimeUnit

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://eerydjnbbmkgipmrjggn.supabase.co",
        supabaseKey = "sb_publishable_je7ZuZ0cmuffR41rG1_WPA_dmegyN51"
    ) {
        install(Auth) {
            sessionManager = SettingsSessionManager()
        }
        install(Postgrest)
        install(Realtime)
        install(Storage)
        httpEngine = OkHttp.create {
            config {
                connectTimeout(60, TimeUnit.SECONDS)
                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(60, TimeUnit.SECONDS)
            }
        }
    }
}
