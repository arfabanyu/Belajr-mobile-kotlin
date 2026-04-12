package com.example.belajr

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.belajr.adapters.PartnerAdapter
import com.example.belajr.adapters.Subject
import com.example.belajr.adapters.SubjectAdapter
import com.example.belajr.models.RelationStatus
import com.example.belajr.views.AuthViewModel
import com.example.belajr.views.MatchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomePage : AppCompatActivity() {

    private lateinit var matchViewModel: MatchViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var partnerAdapter: PartnerAdapter
    private lateinit var subjectAdapter: SubjectAdapter
    private lateinit var layoutEmptyPartners: LinearLayout
    private lateinit var etSearch: EditText
    private lateinit var ivProfile: ImageView
    private var currentKeyword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        layoutEmptyPartners = findViewById(R.id.layoutEmptyPartners)
        etSearch = findViewById(R.id.etSearch)
        ivProfile = findViewById(R.id.ivProfile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        matchViewModel = ViewModelProvider(this)[MatchViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupViews()
        setupSubjectRecyclerView()
        setupPartnerRecyclerView()
        setupSearchView()
        
        NavigationUtils.setupBottomNavigation(this, R.id.nav_discovery)
        
        observeData()

        matchViewModel.loadAllInterests()
        matchViewModel.searchPartners("")
        authViewModel.loadProfile()
    }

    private fun setupViews() {
        ivProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearchView() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                matchViewModel.searchPartners(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSubjectRecyclerView() {
        val rvSubjects = findViewById<RecyclerView>(R.id.rvSubjects)
        
        val initialSubjects = listOf(Subject("Semua"))

        subjectAdapter = SubjectAdapter(initialSubjects) { subject ->
            currentKeyword = if (subject.name == "Semua") "" else subject.name
            etSearch.setText(currentKeyword)
            matchViewModel.searchPartners(currentKeyword)
        }
        rvSubjects.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvSubjects.adapter = subjectAdapter
    }

    private fun setupPartnerRecyclerView() {
        val rvPartners = findViewById<RecyclerView>(R.id.rvPartners)
        partnerAdapter = PartnerAdapter(
            emptyList(),
            onItemClick = { partner ->
                val intent = Intent(this, OtherProfileActivity::class.java).apply {
                    putExtra("USER_ID", partner.profile.id)
                    putExtra("USERNAME", partner.profile.username)
                    putExtra("INTERESTS", partner.profile.interests?.joinToString(", "))
                    putExtra("BIO", partner.profile.learningStatus)
                    putExtra("RELATION_STATUS", partner.relationStatus.name)
                }
                startActivity(intent)
            },
            onActionClick = { partner ->
                when (partner.relationStatus) {
                    RelationStatus.NONE -> {
                        matchViewModel.sendRequest(partner.profile.id, etSearch.text.toString())
                        Toast.makeText(this, getString(R.string.toast_request_sent), Toast.LENGTH_SHORT).show()
                    }
                    RelationStatus.PENDING_OUT -> {
                        matchViewModel.cancelRequest(partner.profile.id, partner.requestId, etSearch.text.toString())
                        Toast.makeText(this, getString(R.string.toast_request_cancelled), Toast.LENGTH_SHORT).show()
                    }
                    RelationStatus.FRIEND -> {
                        val intent = Intent(this, ChatDetailActivity::class.java).apply {
                            putExtra("RECEIVER_ID", partner.profile.id)
                            putExtra("RECEIVER_NAME", partner.profile.username)
                        }
                        startActivity(intent)
                    }
                    RelationStatus.PENDING_IN -> {
                        val intent = Intent(this, OtherProfileActivity::class.java).apply {
                            putExtra("USER_ID", partner.profile.id)
                            putExtra("USERNAME", partner.profile.username)
                            putExtra("RELATION_STATUS", partner.relationStatus.name)
                        }
                        startActivity(intent)
                    }
                }
            }
        )
        rvPartners.layoutManager = LinearLayoutManager(this)
        rvPartners.adapter = partnerAdapter
    }

    private fun observeData() {
        matchViewModel.partners.observe(this) { partners ->
            partnerAdapter.updateData(partners)
            if (partners.isEmpty()) {
                layoutEmptyPartners.visibility = View.VISIBLE
                findViewById<RecyclerView>(R.id.rvPartners).visibility = View.GONE
            } else {
                layoutEmptyPartners.visibility = View.GONE
                findViewById<RecyclerView>(R.id.rvPartners).visibility = View.VISIBLE
            }
        }

        lifecycleScope.launch {
            matchViewModel.interests.collectLatest { interestsList ->
                val dynamicSubjects = mutableListOf(Subject("Semua"))
                interestsList.forEach { interest ->
                    dynamicSubjects.add(Subject(interest))
                }
                subjectAdapter.updateData(dynamicSubjects)
            }
        }

        lifecycleScope.launch {
            authViewModel.profile.collect { profile ->
                profile?.let {
                    Glide.with(this@HomePage)
                        .load(it.avatarUrl)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .circleCrop()
                        .into(ivProfile)
                }
            }
        }

        lifecycleScope.launch {
            matchViewModel.error.collect { error ->
                if (error != null) {
                    Toast.makeText(this@HomePage, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
