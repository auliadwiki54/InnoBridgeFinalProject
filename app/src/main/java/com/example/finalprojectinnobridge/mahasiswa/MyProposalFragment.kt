package com.example.finalprojectinnobridge.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectinnobridge.adapters.ProposalAdapter
import com.example.finalprojectinnobridge.databinding.FragmentMyProposalBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class MyProposalFragment : Fragment() {

    private var _binding: FragmentMyProposalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProposalViewModel by viewModels()
    private lateinit var proposalAdapter: ProposalAdapter

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
        observeViewModel()

        val userId = SessionManager(requireContext()).getUserId() ?: ""
        viewModel.fetchProposalsByUser(userId)
    }

    private fun setupRecyclerView() {
        proposalAdapter = ProposalAdapter(emptyList()) { proposal ->
            // Handle click if needed, e.g., show detail
        }
        binding.rvProposals.apply {
            adapter = proposalAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.proposals.observe(viewLifecycleOwner) { proposals ->
            proposalAdapter.updateData(proposals)
            binding.tvEmpty.visibility = if (proposals.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}