package com.example.belajr

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val username: String,
    val email: String,
    val interests: List<String>? = null,
    val learning_status: String? = null
)

class HomePage : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PartnerAdapter
    private lateinit var chipGroup: ChipGroup

    private val subjects = listOf(
        "Semua", "Matematika", "Fisika", "Kimia", "Biologi",
        "Bahasa Indonesia", "Bahasa Inggris", "Sejarah", "Pemrograman", "Desain"
    )

    private var selectedSubject = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        chipGroup = findViewById(R.id.chipGroup)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PartnerAdapter(emptyList())
        recyclerView.adapter = adapter

        setupChips()
        loadPartners()
    }

    private fun setupChips() {
        subjects.forEach { subject ->
            val chip = Chip(this)
            chip.text = subject
            chip.isCheckable = true
            chip.isChecked = subject == "Semua"
            chip.setOnClickListener {
                selectedSubject = subject
                loadPartners()
            }
            chipGroup.addView(chip)
        }
    }

    private fun loadPartners() {
        val currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: return

        lifecycleScope.launch {
            try {
                val profiles = if (selectedSubject == "Semua") {
                    SupabaseClient.client.postgrest["profiles"]
                        .select {
                            filter { neq("id", currentUserId) }
                        }
                        .decodeList<Profile>()
                } else {
                    SupabaseClient.client.postgrest["profiles"]
                        .select {
                            filter {
                                neq("id", currentUserId)
                                contains("interests", listOf(selectedSubject))
                            }
                        }
                        .decodeList<Profile>()
                }

                adapter.updateData(profiles)

            } catch (e: Exception) {
                Toast.makeText(this@HomePage, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}