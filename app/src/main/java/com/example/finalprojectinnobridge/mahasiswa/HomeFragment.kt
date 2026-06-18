package com.example.finalprojectinnobridge.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.adapters.ChallengeAdapter
import com.example.finalprojectinnobridge.databinding.FragmentHomeBinding
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()

    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCategoryButtons() // Inisialisasi fungsi klik kategori
        observeViewModel()

        viewModel.fetchChallenges()
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(emptyList()) { challenge ->
            val bundle = Bundle().apply {
                putString("challengeId", challenge.challengeId)
            }

            try {
                // 🌟 KUNCI FIX NAVIGASI: ID Action disesuaikan dengan milik HomeFragment di nav_graph.xml
                findNavController().navigate(R.id.action_home_to_challenge_detail, bundle)
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("NAV_ERROR", "Gagal pindah ke detail: ${e.message}")
            }
        }

        // Menggunakan ID XML baru (rv_challenges) dan diatur menjadi Grid 2 Kolom
        binding.rvChallenges.apply {
            adapter = challengeAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    /**
     * Mengatur aksi ketika tombol kategori pada layout diklik oleh pengguna.
     */
    private fun setupCategoryButtons() {
        binding.btnKatLingkungan.setOnClickListener {
            handleCategoryClick("Lingkungan")
        }

        binding.btnKatEnergi.setOnClickListener {
            handleCategoryClick("Energi")
        }

        binding.btnKatIt.setOnClickListener {
            handleCategoryClick("IT")
        }

        binding.btnKatKesehatan.setOnClickListener {
            handleCategoryClick("Kesehatan")
        }

        binding.tvLihatSemua.setOnClickListener {
            handleCategoryClick("Semua")
        }
    }

    /**
     * Memproses filter data berdasarkan nama kategori yang dipilih menggunakan logika lokal runtime
     * sehingga aman tanpa perlu mengubah susunan fungsi di dalam ChallengeViewModel milikmu.
     */
    private fun handleCategoryClick(kategori: String) {
        Toast.makeText(requireContext(), "Menampilkan kategori: $kategori", Toast.LENGTH_SHORT).show()

        val allChallenges = viewModel.challenges.value ?: emptyList()

        if (kategori == "Semua") {
            // Tampilkan kembali seluruh data tantangan tanpa filter
            challengeAdapter.updateData(allChallenges)
        } else {
            // 🌟 KUNCI FIX FILTER: Melakukan filter data secara langsung berdasarkan atribut kategori
            val filteredList = allChallenges.filter {
                it.kategori.contains(kategori, ignoreCase = true)
            }
            challengeAdapter.updateData(filteredList)
        }
    }

    private fun observeViewModel() {
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            challengeAdapter.updateData(challenges)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}