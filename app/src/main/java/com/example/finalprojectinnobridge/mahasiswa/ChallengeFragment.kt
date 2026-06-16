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
    private var currentSelectedCategory: String = "Semua Kategori"

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
     * Menangani input pengetikan di SearchView dan perubahan filter ChipGroup Kategori
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

        // 2. Logika Klik Filter Chip Kategori SDGs
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                currentSelectedCategory = when (checkedIds.first()) {
                    R.id.chip_all -> "Semua Kategori"
                    R.id.chip_lingkungan -> "Lingkungan"
                    R.id.chip_energi -> "Energi"
                    R.id.chip_it -> "IT"
                    else -> "Semua Kategori"
                }
                applyFilterAndSearch()
            }
        }
    }

    /**
     * Fungsi kustom untuk menggabungkan filter kategori dan pencarian teks secara bersamaan
     */
    private fun applyFilterAndSearch() {
        var filteredList = allChallengesList

        // Langkah A: Filter berdasarkan Kategori (mencocokkan item.kategori dari model data kamu)
        if (currentSelectedCategory != "Semua Kategori") {
            filteredList = filteredList.filter { challenge ->
                // Menggunakan .contains untuk antisipasi teks seperti "SDG 9: IT" atau sejenisnya
                challenge.kategori.contains(currentSelectedCategory, ignoreCase = true)
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
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            // Simpan master data asli ke variabel lokal agar bisa difilter berulang kali
            allChallengesList = challenges ?: emptyList()
            applyFilterAndSearch()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}