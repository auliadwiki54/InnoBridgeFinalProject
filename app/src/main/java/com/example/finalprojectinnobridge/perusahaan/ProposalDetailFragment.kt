package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.databinding.FragmentProposalDetailBinding
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class ProposalDetailFragment : Fragment() {

    private var _binding: FragmentProposalDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProposalViewModel by viewModels()
    private var proposalId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProposalDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        proposalId = arguments?.getString("proposalId")
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Hubungi dihilangkan dari detail sesuai permintaan
        binding.btnChatInnovator.visibility = View.GONE

        observeViewModel()
        viewModel.listenToAllProposals()
    }

    private fun observeViewModel() {
        viewModel.proposals.observe(viewLifecycleOwner) { proposals ->
            val proposal = proposals.find { it.proposalId == proposalId }
            proposal?.let { p ->
                binding.tvDetailJudul.text = p.judul
                binding.tvDetailStatus.text = p.status
                binding.tvDetailUser.text = "${p.userName} • ${p.userUniversity}"
                binding.tvDetailSolusi.text = p.solusi
                binding.tvDetailVideo.text = p.pitchVideo.ifEmpty { "Tidak ada video" }
                
                if (p.score > 0 || p.evaluasi.isNotEmpty()) {
                    binding.cardScore.visibility = View.VISIBLE
                    binding.tvDetailScore.text = p.score.toString()
                    binding.tvDetailEvaluasi.text = p.evaluasi
                } else {
                    binding.cardScore.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}