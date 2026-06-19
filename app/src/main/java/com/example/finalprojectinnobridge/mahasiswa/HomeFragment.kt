package com.example.finalprojectinnobridge.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.adapters.ChallengeAdapter
import com.example.finalprojectinnobridge.databinding.FragmentHomeBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()

    private lateinit var challengeAdapter: ChallengeAdapter
    private var activeCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = SessionManager(requireContext()).getUserName() ?: "Mahasiswa"
        binding.tvUserName.text = userName

        setupRecyclerView()
        setupCategoryButtons()
        setupSearch()
        setupStaticCardClicks()
        observeViewModel()

        viewModel.fetchChallenges()
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(emptyList()) { challenge ->
            val bundle = Bundle().apply {
                putString("challengeId", challenge.challengeId)
            }
            try {
                findNavController().navigate(R.id.action_home_to_challenge_detail, bundle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.rvChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupCategoryButtons() {
        binding.btnKatSdg7.setOnClickListener {
            toggleCategory("SDG 7")
        }

        binding.btnKatSdg9.setOnClickListener {
            toggleCategory("SDG 9")
        }

        binding.btnKatSdg11.setOnClickListener {
            toggleCategory("SDG 11")
        }

        binding.btnKatSdg14.setOnClickListener {
            toggleCategory("SDG 14")
        }

        binding.tvLihatSemua.setOnClickListener {
            toggleCategory("Semua")
        }
    }

    private fun toggleCategory(kategori: String) {
        if (kategori == "Semua") {
            activeCategory = null
            handleCategoryClick("Semua")
        } else {
            if (activeCategory == kategori) {
                activeCategory = null
                handleCategoryClick("Semua")
            } else {
                activeCategory = kategori
                handleCategoryClick(kategori)
            }
        }

        // Apply smooth transition animation to category layout
        androidx.transition.TransitionManager.beginDelayedTransition(
            binding.layoutCategoriesContainer,
            androidx.transition.AutoTransition()
        )
        updateCategoryUI()
    }

    private fun updateCategoryUI() {
        // SDG 7
        val isSdg7Active = activeCategory == "SDG 7"
        binding.btnKatSdg7.animate().scaleX(if (isSdg7Active) 1.15f else 1.0f).scaleY(if (isSdg7Active) 1.15f else 1.0f).setDuration(200).start()
        binding.btnKatSdg7.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isSdg7Active) "#F9A825" else "#FFFFFF")
        ))
        binding.ivIconSdg7.imageTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isSdg7Active) "#FFFFFF" else "#F9A825")
        )
        binding.tvDescSdg7.visibility = View.VISIBLE

        // SDG 9
        val isSdg9Active = activeCategory == "SDG 9"
        binding.btnKatSdg9.animate().scaleX(if (isSdg9Active) 1.15f else 1.0f).scaleY(if (isSdg9Active) 1.15f else 1.0f).setDuration(200).start()
        binding.btnKatSdg9.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isSdg9Active) "#DD1367" else "#FFFFFF")
        ))
        binding.ivIconSdg9.imageTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isSdg9Active) "#FFFFFF" else "#DD1367")
        )
        binding.tvDescSdg9.visibility = View.VISIBLE

        // SDG 11
        val isSdg11Active = activeCategory == "SDG 11"
        binding.btnKatSdg11.animate().scaleX(if (isSdg11Active) 1.15f else 1.0f).scaleY(if (isSdg11Active) 1.15f else 1.0f).setDuration(200).start()
        binding.btnKatSdg11.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isSdg11Active) "#FD9D24" else "#FFFFFF")
        ))
        binding.ivIconSdg11.imageTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isSdg11Active) "#FFFFFF" else "#FD9D24")
        )
        binding.tvDescSdg11.visibility = View.VISIBLE

        // SDG 14
        val isSdg14Active = activeCategory == "SDG 14"
        binding.btnKatSdg14.animate().scaleX(if (isSdg14Active) 1.15f else 1.0f).scaleY(if (isSdg14Active) 1.15f else 1.0f).setDuration(200).start()
        binding.btnKatSdg14.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isSdg14Active) "#00A6DD" else "#FFFFFF")
        ))
        binding.ivIconSdg14.imageTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isSdg14Active) "#FFFFFF" else "#00A6DD")
        )
        binding.tvDescSdg14.visibility = View.VISIBLE
    }

    private fun handleCategoryClick(kategori: String) {
        val allChallenges = viewModel.challenges.value ?: emptyList()
        if (kategori == "Semua") {
            challengeAdapter.updateData(allChallenges)
        } else {
            val filteredList = allChallenges.filter {
                it.kategori.contains(kategori, ignoreCase = true)
            }
            challengeAdapter.updateData(filteredList)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                val allChallenges = viewModel.challenges.value ?: emptyList()
                val filteredList = if (query.isEmpty()) {
                    allChallenges
                } else {
                    allChallenges.filter {
                        it.judul.contains(query, ignoreCase = true) ||
                        it.deskripsi.contains(query, ignoreCase = true) ||
                        it.kategori.contains(query, ignoreCase = true)
                    }
                }
                challengeAdapter.updateData(filteredList)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupStaticCardClicks() {
        binding.btnIkutiTantangan.setOnClickListener {
            Toast.makeText(requireContext(), "Membuka halaman semua tantangan...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            challengeAdapter.updateData(challenges)

            // Update dynamic challenge count indicators for each category card
            val totalSdg7 = challenges.count { it.kategori.contains("SDG 7", ignoreCase = true) }
            val totalSdg9 = challenges.count { it.kategori.contains("SDG 9", ignoreCase = true) }
            val totalSdg11 = challenges.count { it.kategori.contains("SDG 11", ignoreCase = true) }
            val totalSdg14 = challenges.count { it.kategori.contains("SDG 14", ignoreCase = true) }

            binding.tvDescSdg7.text = "SDG 7\n$totalSdg7 Tantangan Aktif"
            binding.tvDescSdg9.text = "SDG 9\n$totalSdg9 Tantangan Aktif"
            binding.tvDescSdg11.text = "SDG 11\n$totalSdg11 Tantangan Aktif"
            binding.tvDescSdg14.text = "SDG 14\n$totalSdg14 Tantangan Aktif"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}