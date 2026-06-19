package com.example.finalprojectinnobridge.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.adapters.ProposalAdapter
import com.example.finalprojectinnobridge.databinding.FragmentProfileBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.AuthViewModel
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel
import com.google.firebase.firestore.ListenerRegistration

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private val proposalViewModel: ProposalViewModel by viewModels()

    private lateinit var proposalAdapter: ProposalAdapter
    private var proposalListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserProfileData()
        setupRecyclerView()
        setupNavigation()
        setupLogoutAction()
        observeProposalHistoryLive()
    }

    private fun setupUserProfileData() {
        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId() ?: ""

        authViewModel.fetchUserData(userId)
        authViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.nama
                binding.tvEmail.text = it.email
                binding.tvRole.text = it.role
                binding.tvDescription.text = it.bio.ifEmpty { "Innovation Enthusiast" }
                
                binding.tvUniversity.text = it.universitas.ifEmpty { "Belum diatur" }
                binding.tvMajor.text = it.jurusan.ifEmpty { "Belum diatur" }
                binding.tvSkills.text = it.keahlian.ifEmpty { "Belum diatur" }
            }
        }
    }

    private fun setupRecyclerView() {
        proposalAdapter = ProposalAdapter(
            list = emptyList(),
            onDetailClick = { proposal ->
                val bundle = Bundle().apply {
                    putString("proposalId", proposal.proposalId)
                }
                findNavController().navigate(R.id.action_navigation_profile_to_navigation_proposal_detail, bundle)
            }
        )

        binding.rvInnovationHistory.apply {
            adapter = proposalAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupNavigation() {
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_editProfileFragment)
        }
    }

    private fun observeProposalHistoryLive() {
        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId() ?: ""

        proposalListener = com.example.finalprojectinnobridge.repositories.ProposalRepository().listenProposalsByUser(userId) { listProposal, error ->
            if (error == null) {
                proposalAdapter.updateData(listProposal)
                
                val totalSubmitted = listProposal.size
                val acceptedSubmitted = listProposal.count { it.status.equals("Diterima", ignoreCase = true) }
                
                binding.tvProposalVal.text = totalSubmitted.toString()
                
                // Real-time XP: 100 XP per proposal submitted + 500 XP per accepted proposal
                val xp = (totalSubmitted * 100) + (acceptedSubmitted * 500)
                binding.tvXpVal.text = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(xp)
                
                // Real-time Level: 1 + (XP / 1000)
                val level = 1 + (xp / 1000)
                binding.tvLevel.text = "Lvl $level"
                
                // Real-time Impact Score: (Accepted / Total) * 100%
                val impactScore = if (totalSubmitted > 0) {
                    (acceptedSubmitted.toDouble() / totalSubmitted * 100).toInt()
                } else {
                    0
                }
                binding.tvImpactVal.text = "$impactScore%"
            }
        }
    }

    private fun setupLogoutAction() {
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            val sessionManager = SessionManager(requireContext())
            sessionManager.clearSession()
            
            Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()
            
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            
            findNavController().navigate(R.id.fragment_login, null, navOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        proposalListener?.remove()
        _binding = null
    }
}