package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectinnobridge.adapters.ProposalAdapter
import com.example.finalprojectinnobridge.databinding.FragmentProposalListBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class ProposalListFragment : Fragment() {

    private var _binding: FragmentProposalListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProposalViewModel by viewModels()
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

        setupRecyclerView()
        observeViewModel()

        // In a real app, we might want to fetch proposals for all challenges owned by this company
        // For now, let's fetch all (or refine by challenge if passed in arguments)
        viewModel.fetchProposalsByUser("") // This is a placeholder, should be by company
    }

    private fun setupRecyclerView() {
        proposalAdapter = ProposalAdapter(emptyList()) { proposal ->
            // Navigate to proposal detail or change status
        }
        binding.rvProposals.apply {
            adapter = proposalAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.proposals.observe(viewLifecycleOwner) { proposals ->
            proposalAdapter.updateData(proposals)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}