package com.example.finalprojectinnobridge.perusahaan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private var currentPdfUrl: String? = null

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

        binding.btnViewPdf.setOnClickListener {
            if (!currentPdfUrl.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(currentPdfUrl), "application/pdf")
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    // Jika tidak ada PDF viewer, buka di browser
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentPdfUrl))
                    startActivity(browserIntent)
                }
            } else {
                Toast.makeText(requireContext(), "Berkas PDF tidak tersedia", Toast.LENGTH_SHORT).show()
            }
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
                currentPdfUrl = p.pdfUrl
                binding.tvDetailJudul.text = p.judul
                binding.tvDetailStatus.text = p.status
                binding.tvDetailUser.text = "${p.userName} • ${p.userUniversity}"
                binding.tvDetailSolusi.text = p.solusi
                binding.tvDetailVideo.text = p.pitchVideo.ifEmpty { "Tidak ada video" }
                
                binding.btnViewPdf.visibility = if (p.pdfUrl.isNotEmpty()) View.VISIBLE else View.GONE
                
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