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

        val userId = SessionManager(requireContext()).getUserId() ?: ""
        challengeViewModel.fetchChallenges()
        
        binding.btnAddChallenge.setOnClickListener {
            findNavController().navigate(R.id.navigation_add_challenge)
        }
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
        challengeViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val myChallenges = challenges.filter { it.perusahaanId == userId }
            challengeAdapter.updateData(myChallenges)
            binding.tvActiveCount.text = myChallenges.count { it.status == "Aktif" }.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}