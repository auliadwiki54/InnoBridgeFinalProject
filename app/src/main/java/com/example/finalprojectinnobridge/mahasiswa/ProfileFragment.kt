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
            }
        }

        binding.tvXpVal.text = "8.450"
        binding.tvImpactVal.text = "92%"
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
                binding.tvProposalVal.text = listProposal.size.toString()
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