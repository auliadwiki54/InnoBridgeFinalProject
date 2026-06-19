package com.example.finalprojectinnobridge.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.adapters.ChallengeAdapter
import com.example.finalprojectinnobridge.databinding.FragmentChallengeBinding
import com.example.finalprojectinnobridge.models.Challenge
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel

class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()
    private lateinit var challengeAdapter: ChallengeAdapter

    // List cadangan untuk menyimpan data asli dari ViewModel agar filter tidak merusak data awal
    private var allChallengesList: List<Challenge> = emptyList()
    private var currentSearchQuery: String = ""
    private var activeCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.fetchChallenges()
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(emptyList()) { challenge ->
            val bundle = Bundle().apply {
                putString("challengeId", challenge.challengeId)
            }
            findNavController().navigate(R.id.action_challenge_to_detail, bundle)
        }
        binding.rvChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    /**
     * Menangani input pengetikan di SearchView dan perubahan filter Kategori
     */
    private fun setupListeners() {
        // 1. Logika Pencarian Tantangan (Real-time secara lokal)
        binding.svChallenges.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText.orEmpty().trim()
                applyFilterAndSearch()
                return true
            }
        })

        // 2. Logika Klik Kategori SDGs
        binding.btnKatAll.setOnClickListener {
            toggleCategory(null)
        }
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
    }

    private fun toggleCategory(kategori: String?) {
        if (kategori == null) {
            activeCategory = null
        } else {
            if (activeCategory == kategori) {
                activeCategory = null
            } else {
                activeCategory = kategori
            }
        }

        // Apply smooth transition animation to category layout
        androidx.transition.TransitionManager.beginDelayedTransition(
            binding.layoutCategoriesContainer,
            androidx.transition.AutoTransition()
        )
        updateCategoryUI()
        applyFilterAndSearch()
    }

    private fun updateCategoryUI() {
        // ALL
        val isAllActive = activeCategory == null
        binding.btnKatAll.animate().scaleX(if (isAllActive) 1.15f else 1.0f).scaleY(if (isAllActive) 1.15f else 1.0f).setDuration(200).start()
        binding.btnKatAll.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isAllActive) "#0056D2" else "#FFFFFF")
        ))
        binding.ivIconAll.imageTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (isAllActive) "#FFFFFF" else "#0056D2")
        )
        binding.tvDescAll.visibility = View.VISIBLE

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

    /**
     * Fungsi kustom untuk menggabungkan filter kategori dan pencarian teks secara bersamaan
     */
    private fun applyFilterAndSearch() {
        var filteredList = allChallengesList

        // Langkah A: Filter berdasarkan Kategori
        val category = activeCategory
        if (category != null) {
            filteredList = filteredList.filter { challenge ->
                challenge.kategori.contains(category, ignoreCase = true)
            }
        }

        // Langkah B: Filter berdasarkan teks judul/deskripsi di SearchView
        if (currentSearchQuery.isNotEmpty()) {
            filteredList = filteredList.filter { challenge ->
                challenge.judul.contains(currentSearchQuery, ignoreCase = true) ||
                        challenge.deskripsi.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        // Kirim list hasil gabungan filter ke Adapter
        challengeAdapter.updateData(filteredList)

        // Sembunyikan Tantangan Populer jika sedang mencari atau menggunakan filter
        val isFiltering = activeCategory != null || currentSearchQuery.isNotEmpty()
        binding.tvPopularHeader.visibility = if (isFiltering) View.GONE else View.VISIBLE
        binding.cvPopularChallenge.visibility = if (isFiltering) View.GONE else View.VISIBLE
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            // Simpan master data asli ke variabel lokal agar bisa difilter berulang kali
            allChallengesList = challenges ?: emptyList()
            
            // Update dynamic challenge count indicators for each category card
            val totalSdg7 = allChallengesList.count { it.kategori.contains("SDG 7", ignoreCase = true) }
            val totalSdg9 = allChallengesList.count { it.kategori.contains("SDG 9", ignoreCase = true) }
            val totalSdg11 = allChallengesList.count { it.kategori.contains("SDG 11", ignoreCase = true) }
            val totalSdg14 = allChallengesList.count { it.kategori.contains("SDG 14", ignoreCase = true) }
            val totalAll = allChallengesList.size

            binding.tvDescAll.text = "Semua\n$totalAll Tantangan"
            binding.tvDescSdg7.text = "SDG 7\n$totalSdg7 Tantangan"
            binding.tvDescSdg9.text = "SDG 9\n$totalSdg9 Tantangan"
            binding.tvDescSdg11.text = "SDG 11\n$totalSdg11 Tantangan"
            binding.tvDescSdg14.text = "SDG 14\n$totalSdg14 Tantangan"

            updateCategoryUI()
            applyFilterAndSearch()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}