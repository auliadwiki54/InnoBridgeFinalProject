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
            // Navigasi ke detail tantangan.
            findNavController().navigate(R.id.action_challenge_to_detail, bundle)
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
     * Memproses filter data berdasarkan nama kategori yang dipilih.
     */
    private fun handleCategoryClick(kategori: String) {
        Toast.makeText(requireContext(), "Kategori $kategori dipilih", Toast.LENGTH_SHORT).show()

        // JIKA VIEWMODEL ANDA MEMILIKI FUNGSI FILTER, AKTIFKAN LOGIKA INI:
        // if (kategori == "Semua") {
        //     viewModel.fetchChallenges()
        // } else {
        //     viewModel.fetchChallengesByCategory(kategori)
        // }
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