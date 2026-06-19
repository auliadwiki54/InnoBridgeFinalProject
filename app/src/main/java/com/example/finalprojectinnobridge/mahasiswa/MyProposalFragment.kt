package com.example.finalprojectinnobridge.mahasiswa

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
import com.example.finalprojectinnobridge.databinding.FragmentMyProposalBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class MyProposalFragment : Fragment() {

    private var _binding: FragmentMyProposalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProposalViewModel by viewModels()
    private lateinit var proposalAdapter: ProposalAdapter
    private var allProposalsList: List<com.example.finalprojectinnobridge.models.Proposal> = emptyList()
    private var currentFilterStatus: String = "Semua"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProposalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterListeners()
        observeViewModel()

        val userId = SessionManager(requireContext()).getUserId() ?: ""
        viewModel.listenToUserProposals(userId)
    }

    private fun setupRecyclerView() {
        proposalAdapter = ProposalAdapter(
            list = emptyList(),
            onDetailClick = { proposal ->
                val bundle = Bundle().apply {
                    putString("proposalId", proposal.proposalId)
                }
                findNavController().navigate(R.id.action_navigation_my_proposal_to_navigation_proposal_detail, bundle)
            }
        )
        binding.rvProposals.apply {
            adapter = proposalAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFilterListeners() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                currentFilterStatus = when (checkedIds.first()) {
                    R.id.chip_all -> "Semua"
                    R.id.chip_pending -> "Pending"
                    R.id.chip_diterima -> "Diterima"
                    R.id.chip_ditolak -> "Ditolak"
                    else -> "Semua"
                }
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val filteredList = if (currentFilterStatus == "Semua") {
            allProposalsList
        } else {
            allProposalsList.filter { it.status.equals(currentFilterStatus, ignoreCase = true) }
        }
        proposalAdapter.updateData(filteredList)
        binding.tvEmpty.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.userProposals.observe(viewLifecycleOwner) { proposals ->
            allProposalsList = proposals ?: emptyList()
            applyFilter()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}