package com.example.finalprojectinnobridge.perusahaan

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

        setupRecyclerView()
        loadUserData(userId)
        loadStats(userId)
        setupNavigation()

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            sessionManager.clearSession()
            Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.fragment_login, null, navOptions)
        }
    }

    private fun loadUserData(userId: String) {
        authViewModel.fetchUserData(userId)
        authViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.nama
                binding.tvEmail.text = it.email
                // tvRole ada di layout — set ke role dari data
                binding.tvRole.text = it.role

                // Bind new dynamic company details fields
                binding.tvCompanyDesc.text = it.bio.ifEmpty { "Belum diatur" }
                binding.tvCompanyIndustry.text = it.industri.ifEmpty { "Belum diatur" }
                binding.tvCompanyWebsite.text = it.website.ifEmpty { "Belum diatur" }
                binding.tvCompanyAddress.text = it.alamat.ifEmpty { "Belum diatur" }
            }
        }
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(emptyList()) { }
        binding.rvMyChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadStats(userId: String) {
        // Observe challenges dulu — tidak nested dengan proposals
        challengeViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val myChallenges = challenges.filter { it.perusahaanId == userId }
            binding.tvActiveChallenges.text = myChallenges.size.toString()
            challengeAdapter.updateData(myChallenges)
        }

        // Observe proposals secara terpisah
        proposalViewModel.proposals.observe(viewLifecycleOwner) { allProposals ->
            // Hitung proposal yang masuk untuk challenge milik perusahaan ini
            val myChallengeIds = challengeViewModel.challenges.value
                ?.filter { it.perusahaanId == userId }
                ?.map { it.challengeId }
                ?: emptyList()
            val receivedProposals = allProposals.filter { it.challengeId in myChallengeIds }
            binding.tvTotalProposals.text = receivedProposals.size.toString()
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
