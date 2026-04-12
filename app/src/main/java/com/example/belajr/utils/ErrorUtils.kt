package com.example.belajr.utils

object ErrorUtils {
    fun parseErrorMessage(technicalError: String): String {
        return when {
            technicalError.contains("Invalid login credentials", ignoreCase = true) -> 
                "Email atau password salah. Silakan coba lagi."
            
            technicalError.contains("Email not confirmed", ignoreCase = true) -> 
                "Email kamu belum dikonfirmasi. Cek kotak masuk atau spam di email kamu."
            
            technicalError.contains("User already registered", ignoreCase = true) || 
            technicalError.contains("already exists", ignoreCase = true) ||
            technicalError.contains("Email already in use", ignoreCase = true) -> 
                "Email sudah terdaftar. Gunakan email lain atau silakan login."
            
            technicalError.contains("Password should be at least", ignoreCase = true) ||
            technicalError.contains("weak password", ignoreCase = true) -> 
                "Password terlalu lemah atau pendek. Gunakan minimal 6 karakter dengan kombinasi angka/huruf."
            
            technicalError.contains("Unable to resolve host", ignoreCase = true) || 
            technicalError.contains("timeout", ignoreCase = true) -> 
                "Koneksi internet bermasalah. Pastikan kamu terhubung ke internet."
            
            technicalError.contains("Rate limit exceeded", ignoreCase = true) -> 
                "Terlalu banyak mencoba. Silakan tunggu beberapa saat lagi."
                
            technicalError.contains("Invalid email", ignoreCase = true) ||
            technicalError.contains("unable to validate email address", ignoreCase = true) ||
            technicalError.contains("validation failed", ignoreCase = true) ->
                "Format email tidak valid atau tidak dapat diverifikasi. Pastikan penulisan email sudah benar."

            else -> "Terjadi kesalahan: $technicalError"
        }
    }
}
