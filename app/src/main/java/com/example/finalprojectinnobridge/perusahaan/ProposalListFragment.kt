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
    
    private var selectedChallengeId: String = "all"
    private var selectedStatus: String = "Semua Status"
    
    private val challengeIds = mutableListOf<String>()
    private val challengeTitles = mutableListOf<String>()
    private val statusOptions = listOf("Semua Status", "Pending", "Review", "Diterima", "Ditolak")

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
        setupFilters(userId)
        
        challengeViewModel.fetchChallenges()
        proposalViewModel.listenToAllProposals()
    }

    private fun setupFilters(userId: String) {
        // Setup Status Spinner
        val statusAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerStatus.adapter = statusAdapter
        binding.spinnerStatus.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedStatus = statusOptions[position]
                applyFilters(userId)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Observe challenges to populate challenge filter
        challengeViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val myChallenges = challenges.filter { it.perusahaanId == userId }
            
            challengeIds.clear()
            challengeTitles.clear()
            
            challengeIds.add("all")
            challengeTitles.add("Semua Tantangan")
            
            myChallenges.forEach {
                challengeIds.add(it.challengeId)
                challengeTitles.add(it.judul)
            }

            val challengeAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, challengeTitles).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.spinnerChallenge.adapter = challengeAdapter
            
            // Re-select currently selected challenge if still present
            val index = challengeIds.indexOf(selectedChallengeId)
            if (index >= 0) {
                binding.spinnerChallenge.setSelection(index)
            } else {
                selectedChallengeId = "all"
                binding.spinnerChallenge.setSelection(0)
            }
        }

        binding.spinnerChallenge.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < challengeIds.size) {
                    selectedChallengeId = challengeIds[position]
                    applyFilters(userId)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Observe proposals to trigger filter updates
        proposalViewModel.proposals.observe(viewLifecycleOwner) {
            applyFilters(userId)
        }
    }

    private fun applyFilters(userId: String) {
        val challenges = challengeViewModel.challenges.value ?: emptyList()
        val myChallengeIds = challenges.filter { it.perusahaanId == userId }.map { it.challengeId }
        val allProposals = proposalViewModel.proposals.value ?: emptyList()

        val filteredProposals = allProposals.filter { proposal ->
            val matchesChallenge = if (selectedChallengeId == "all") {
                proposal.challengeId in myChallengeIds
            } else {
                proposal.challengeId == selectedChallengeId
            }

            val matchesStatus = if (selectedStatus == "Semua Status") {
                true
            } else {
                proposal.status.equals(selectedStatus, ignoreCase = true)
            }

            matchesChallenge && matchesStatus
        }

        proposalAdapter.updateData(filteredProposals)
        binding.layoutEmptyState.visibility = if (filteredProposals.isEmpty()) View.VISIBLE else View.GONE
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