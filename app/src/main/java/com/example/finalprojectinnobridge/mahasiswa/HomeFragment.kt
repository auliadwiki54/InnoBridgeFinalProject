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
import com.example.finalprojectinnobridge.adapters.ChallengeAdapter
import com.example.finalprojectinnobridge.databinding.FragmentHomeBinding
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()
    
    private lateinit var recommendedAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        
        viewModel.fetchChallenges()
    }

    private fun setupRecyclerView() {
        recommendedAdapter = ChallengeAdapter(emptyList()) { challenge ->
            val bundle = Bundle().apply {
                putString("challengeId", challenge.challengeId)
            }
            // Navigate to detail. Ensure the action ID exists in your nav graph.
            findNavController().navigate(R.id.action_challenge_to_detail, bundle)
        }
        binding.rvRecommended.apply {
            adapter = recommendedAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            recommendedAdapter.updateData(challenges)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
