package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.adapters.ChallengeAdapter
import com.example.finalprojectinnobridge.databinding.FragmentDashboardBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val challengeViewModel: ChallengeViewModel by viewModels()
    private val proposalViewModel: ProposalViewModel by viewModels()
    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // Memicu pengambilan data master tantangan dari database
        challengeViewModel.fetchChallenges()
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(emptyList()) { challenge ->
            val bundle = Bundle().apply {
                putString("challengeId", challenge.challengeId)
            }
            findNavController().navigate(R.id.navigation_edit_challenge, bundle)
        }

        binding.rvMyChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        val userId = SessionManager(requireContext()).getUserId() ?: ""

        // Buat list penampung ID tantangan milik perusahaan ini agar bisa diakses oleh observer proposal
        val myChallengeIds = mutableListOf<String>()

        // 1. Mengamati data Tantangan (Challenges)
        challengeViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            // Saring tantangan yang dibuat oleh ID Perusahaan yang sedang login
            val myChallenges = challenges.filter { it.perusahaanId == userId }
            challengeAdapter.updateData(myChallenges)

            // Simpan semua ID tantangan milik kita ke dalam list penampung
            myChallengeIds.clear()
            myChallengeIds.addAll(myChallenges.map { it.challengeId })

            // Set teks indikator jumlah tantangan aktif
            binding.tvActiveCount.text = myChallenges.count { it.status == "Aktif" }.toString()
        }

        // 2. Mengamati data Proposal Masuk (Menghitung Total Pelamar Berdasarkan challengeId)
        proposalViewModel.proposals.observe(viewLifecycleOwner) { proposals ->
            // FIX LOGIC: Hitung proposal yang nilai challengeId-nya ada di dalam daftar tantangan perusahaan kita
            val totalApplicants = proposals.count { it.challengeId in myChallengeIds }
            binding.tvTotalApplicantsCount.text = totalApplicants.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}