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
import com.example.finalprojectinnobridge.adapters.ProposalAdapter
import com.example.finalprojectinnobridge.databinding.FragmentProposalListBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class ProposalListFragment : Fragment() {

    private var _binding: FragmentProposalListBinding? = null
    private val binding get() = _binding!!
    
    private val proposalViewModel: ProposalViewModel by viewModels()
    private val challengeViewModel: ChallengeViewModel by viewModels()
    
    private lateinit var proposalAdapter: ProposalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProposalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = SessionManager(requireContext()).getUserId() ?: ""

        setupRecyclerView()
        
        challengeViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val myChallengeIds = challenges.filter { it.perusahaanId == userId }.map { it.challengeId }
            
            proposalViewModel.proposals.observe(viewLifecycleOwner) { allProposals ->
                val filteredProposals = allProposals.filter { it.challengeId in myChallengeIds }
                proposalAdapter.updateData(filteredProposals)
                
                binding.tvEmpty.visibility = if (filteredProposals.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        challengeViewModel.fetchChallenges()
        proposalViewModel.listenToAllProposals()
    }

    private fun setupRecyclerView() {
        proposalAdapter = ProposalAdapter(
            list = emptyList(),
            onDetailClick = { proposal ->
                val bundle = Bundle().apply {
                    putString("proposalId", proposal.proposalId)
                }
                findNavController().navigate(R.id.action_navigation_proposal_list_to_navigation_proposal_detail, bundle)
            },
            onScoreClick = { proposal ->
                val bundle = Bundle().apply {
                    putString("proposalId", proposal.proposalId)
                    putString("judul", proposal.judul)
                    putString("userName", proposal.userName)
                }
                findNavController().navigate(R.id.action_navigation_proposal_list_to_navigation_give_score, bundle)
            },
            onContactClick = { proposal ->
                val bundle = Bundle().apply {
                    putString("partnerId", proposal.userId)
                    putString("partnerName", proposal.userName)
                }
                try {
                    findNavController().navigate(R.id.action_navigation_proposal_list_to_navigation_chat_room, bundle)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
        
        binding.rvProposals.apply {
            adapter = proposalAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}