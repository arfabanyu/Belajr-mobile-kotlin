package com.example.belajr

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.belajr.models.ProfileUpdate
import com.example.belajr.views.AuthState
import com.example.belajr.views.AuthViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var etUsername: EditText
    private lateinit var etBio: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSave: TextView
    private lateinit var cgInterests: ChipGroup
    private lateinit var imgProfile: ShapeableImageView
    
    private var selectedImageUri: Uri? = null
    private var isUpdating = false // Tambahkan flag ini

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imgProfile.setImageURI(it)
        }
    }

    private val allSubjects = listOf(
        "Matematika", "Bahasa Indonesia", "Bahasa Inggris", "Fisika", "Kimia", "Biologi",
        "Ekonomi", "Geografi", "Sosiologi", "Sejarah", "Informatika", "Seni Budaya",
        "PJOK", "PAI", "PKN", "Antropologi", "Bahasa Arab", "Bahasa Mandarin", "Bahasa Jepang",
        "Rekayasa Perangkat Lunak (RPL)", "Teknik Komputer Jaringan (TKJ)", "Multimedia", 
        "Sistem Informatika, Jaringan & Aplikasi (SIJA)", "Cyber Security", "Animasi",
        "Desain Komunikasi Visual (DKV)", "Produksi Film & Televisi",
        "Teknik Kendaraan Ringan (TKR)", "Teknik Sepeda Motor (TSM)", "Teknik Alat Berat",
        "Teknik Pemesinan (TP)", "Teknik Pengelasan (Las)", "Teknik Mekatronika",
        "Teknik Audio Video (TAV)", "Teknik Pesawat Udara", "Teknik Konstruksi Kapal",
        "Teknik Instalasi Tenaga Listrik (TITL)", "Teknik Otomasi Industri", 
        "Teknik Pendingin & Tata Udara", "Teknik Energi Terbarukan",
        "Bisnis Konstruksi & Properti (BKP)", "Desain Pemodelan & Informasi Bangunan (DPIB)",
        "Teknik Geomatika (Surveyor)", "Geologi Pertambangan",
        "Akuntansi & Keuangan Lembaga", "Otomatisasi & Tata Kelola Perkantoran", 
        "Bisnis Daring & Pemasaran", "Perbankan Syariah", "Logistik",
        "Kuliner / Tata Boga", "Tata Busana (Fashion Design)", "Perhotelan", 
        "Wisata & Perjalanan", "Kecantikan & Tata Rias", "Kriya Kreatif Batik",
        "Kriya Kreatif Keramik", "Kriya Kreatif Logam",
        "Asisten Keperawatan", "Farmasi Klinis & Komunitas", "Farmasi Industri",
        "Dental Asisten", "Teknologi Laboratorium Medik", "Pekerjaan Sosial",
        "Agribisnis Tanaman Pangan & Hortikultura", "Agribisnis Ternak Unggas",
        "Teknologi Hasil Pertanian", "Perhutanan", "Nautika Kapal Niaga", 
        "Teknika Kapal Penangkap Ikan", "Budidaya Perikanan"
    )

    private val selectedInterests = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupViews()
        observeViewModel()

        authViewModel.loadProfile()
    }

    private fun setupViews() {
        etUsername = findViewById(R.id.etUsername)
        etBio = findViewById(R.id.etBio)
        etEmail = findViewById(R.id.etEmail)
        btnSave = findViewById(R.id.btnSave)
        cgInterests = findViewById(R.id.cgInterests)
        imgProfile = findViewById(R.id.imgProfilePicture)
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        populateInterests()

        findViewById<android.view.View>(R.id.btnChangePhoto).setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val username = etUsername.text.toString().trim()
        val bio = etBio.text.toString().trim()

        if (username.isEmpty()) {
            etUsername.error = "Username tidak boleh kosong"
            return
        }

        isUpdating = true // Tandai bahwa kita sedang melakukan update
        
        lifecycleScope.launch {
            var avatarUrl: String? = authViewModel.profile.value?.avatarUrl
            val oldAvatarUrl = avatarUrl

            if (selectedImageUri != null) {
                val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                    authViewModel.uploadAvatar(bytes, fileName).onSuccess { newUrl ->
                        avatarUrl = newUrl
                        oldAvatarUrl?.let { url ->
                            if (url.isNotEmpty() && url.contains("/")) {
                                val oldFileName = url.substringAfterLast("/")
                                authViewModel.deleteOldAvatar(oldFileName)
                            }
                        }
                    }.onFailure {
                        Toast.makeText(this@EditProfileActivity, "Gagal upload foto: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            authViewModel.updateProfile(ProfileUpdate(
                username = username,
                learningStatus = bio,
                interests = selectedInterests.toList(),
                avatarUrl = avatarUrl
            ))
        }
    }

    private fun populateInterests() {
        cgInterests.removeAllViews()
        for (subject in allSubjects) {
            val chip = Chip(this)
            chip.text = subject
            chip.isCheckable = true
            chip.setChipBackgroundColorResource(R.color.bg_light)
            chip.setTextColor(getColor(R.color.primary))
            
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedInterests.add(subject)
                } else {
                    selectedInterests.remove(subject)
                }
            }
            cgInterests.addView(chip)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.profile.collect { profile ->
                    profile?.let {
                        etUsername.setText(it.username)
                        etBio.setText(it.learningStatus ?: "") 
                        etEmail.setText(it.email)
                        etEmail.isEnabled = false
                        
                        if (!it.avatarUrl.isNullOrEmpty()) {
                            Glide.with(this@EditProfileActivity)
                                .load(it.avatarUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(imgProfile)
                        } else {
                            imgProfile.setImageResource(R.drawable.default_profile)
                        }
                        
                        it.interests?.forEach { interest ->
                            for (i in 0 until cgInterests.childCount) {
                                val chip = cgInterests.getChildAt(i) as Chip
                                if (chip.text == interest) {
                                    chip.isChecked = true
                                    selectedInterests.add(interest)
                                }
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            btnSave.isEnabled = false
                            btnSave.alpha = 0.5f
                        }
                        is AuthState.Success -> {
                            btnSave.isEnabled = true
                            btnSave.alpha = 1.0f
                            // HANYA finish jika status Success dipicu oleh klik tombol simpan
                            if (isUpdating) {
                                isUpdating = false
                                Toast.makeText(this@EditProfileActivity, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                        is AuthState.Error -> {
                            isUpdating = false
                            btnSave.isEnabled = true
                            btnSave.alpha = 1.0f
                            Toast.makeText(this@EditProfileActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            btnSave.isEnabled = true
                            btnSave.alpha = 1.0f
                        }
                    }
                }
            }
        }
    }
}
