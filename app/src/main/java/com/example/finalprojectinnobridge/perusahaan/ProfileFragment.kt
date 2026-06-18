package com.example.finalprojectinnobridge.perusahaan

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
import com.example.finalprojectinnobridge.databinding.FragmentProfilePerusahaanBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.AuthViewModel
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfilePerusahaanBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by viewModels()
    private val challengeViewModel: ChallengeViewModel by viewModels()
    private val proposalViewModel: ProposalViewModel by viewModels()
    
    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePerusahaanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId() ?: ""

        setupUserData(userId)
        setupRecyclerView()
        setupStatsLive(userId)
        setupNavigation()

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            sessionManager.clearSession()
            Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()
            requireActivity().finishAffinity()
        }
    }

    private fun setupUserData(userId: String) {
        authViewModel.fetchUserData(userId)
        authViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.nama
                binding.tvEmail.text = it.email
                // binding.tvRole.text = it.role // Jika ada di XML
            }
        }
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(emptyList()) { challenge ->
            // Action on click (optional)
        }
        binding.rvMyChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupStatsLive(userId: String) {
        // Listen to challenges to count active ones and filter proposals
        challengeViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val myChallenges = challenges.filter { it.perusahaanId == userId }
            binding.tvActiveChallenges.text = myChallenges.size.toString()
            challengeAdapter.updateData(myChallenges)

            val myChallengeIds = myChallenges.map { it.challengeId }
            
            // Listen to all proposals and filter those belonging to this company's challenges
            proposalViewModel.proposals.observe(viewLifecycleOwner) { allProposals ->
                val receivedProposals = allProposals.filter { it.challengeId in myChallengeIds }
                binding.tvTotalProposals.text = receivedProposals.size.toString()
            }
        }
        
        challengeViewModel.fetchChallenges()
        proposalViewModel.listenToAllProposals()
    }

    private fun setupNavigation() {
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_perusahaan_to_editProfileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}