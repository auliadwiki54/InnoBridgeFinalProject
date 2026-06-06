package com.example.finalprojectinnobridge.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.FragmentChallengeDetailBinding
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel

class ChallengeDetailFragment : Fragment() {

    private var _binding: FragmentChallengeDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()
    private var challengeId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challengeId = arguments?.getString("challengeId")

        setupToolbar()
        observeViewModel()

        binding.btnAjukan.setOnClickListener {
            val bundle = Bundle().apply {
                putString("challengeId", challengeId)
            }
            findNavController().navigate(R.id.action_detail_to_submit_proposal, bundle)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val challenge = challenges.find { it.challengeId == challengeId }
            challenge?.let {
                binding.tvJudul.text = it.judul
                binding.tvKategori.text = it.kategori
                binding.tvDeskripsi.text = it.deskripsi
                binding.tvReward.text = it.reward
                binding.tvDeadline.text = it.deadline
                // binding.tvPerusahaan.text = it.perusahaanId // In real app, fetch company name
            }
        }
        viewModel.fetchChallenges()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}