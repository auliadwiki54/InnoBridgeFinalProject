package com.example.finalprojectinnobridge.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.FragmentChallengeDetailBinding
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel

class ChallengeDetailFragment : Fragment() {

    private var _binding: FragmentChallengeDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()
    private var challengeId: String? = null

    private var targetPerusahaanId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengambil kiriman data ID tantangan dari adapter beranda
        challengeId = arguments?.getString("challengeId")

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Tombol Ajukan Proposal
        binding.btnAjukan.setOnClickListener {
            val bundle = Bundle().apply {
                putString("challengeId", challengeId)
            }
            findNavController().navigate(R.id.action_detail_to_submit_proposal, bundle)
        }

        // Tombol Chat Perusahaan Mitra
        binding.btnChatPerusahaan.setOnClickListener {
            if (targetPerusahaanId.isNotBlank()) {
                val bundle = Bundle().apply {
                    putString("partnerId", targetPerusahaanId)
                }
                try {
                    // Berpindah aman ke ruang obrolan bersama
                    findNavController().navigate(R.id.action_detail_to_chat_room, bundle)
                } catch (e: IllegalArgumentException) {
                    android.util.Log.e("NAV_ERROR", "Navigasi gagal: ${e.message}")
                }
            } else {
                Toast.makeText(requireContext(), "Mohon tunggu, data mitra sedang dimuat...", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol Download Berkas Tambahan
        binding.btnDownloadResource.setOnClickListener {
            Toast.makeText(requireContext(), "Mengunduh file pendukung tantangan...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val challenge = challenges.find { it.challengeId == challengeId }
            challenge?.let {
                targetPerusahaanId = it.perusahaanId.trim()

                // Sinkronisasi data model ke komponen tampilan xml
                binding.tvJudul.text = it.judul
                binding.tvKategori.text = it.kategori
                binding.tvDeskripsi.text = it.deskripsi
                binding.tvReward.text = it.reward
                binding.tvDeadline.text = it.deadline
                binding.tvPerusahaan.text = "Oleh Perusahaan ID: $targetPerusahaanId"
                binding.tvLisensiDetail.text = "Skema Lisensi: ${it.skemaLisensi}. Hak cipta solusi dilindungi sepenuhnya oleh sistem inovasi platform."
            }
        }
        viewModel.fetchChallenges()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}