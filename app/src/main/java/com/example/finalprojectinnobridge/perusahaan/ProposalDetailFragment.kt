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
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.FragmentProposalDetailBinding
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.SessionManager
import java.util.Locale

class ProposalDetailFragment : Fragment() {

    private var _binding: FragmentProposalDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProposalViewModel by viewModels()
    private val challengeViewModel: ChallengeViewModel by viewModels()
    
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

        observeViewModel()
        viewModel.listenToAllProposals()
    }

    private fun observeViewModel() {
        val sessionManager = SessionManager(requireContext())
        val role = sessionManager.getUserRole()

        viewModel.proposals.observe(viewLifecycleOwner) { proposals ->
            val proposal = proposals.find { it.proposalId == proposalId }
            proposal?.let { p ->
                currentPdfUrl = p.pdfUrl
                binding.tvDetailJudul.text = p.judul
                binding.tvDetailStatus.text = p.status
                binding.tvDetailUserName.text = p.userName.ifEmpty { "Inovator" }
                binding.tvDetailUserUniversity.text = p.userUniversity.ifEmpty { "Universitas" }
                binding.tvDetailSolusi.text = p.solusi
                
                // Color status badge dynamically
                val statusBgColor = when (p.status.lowercase(Locale.getDefault())) {
                    "diterima" -> R.color.status_diterima_bg
                    "ditolak" -> R.color.status_ditolak_bg
                    "review" -> R.color.status_review_bg
                    else -> R.color.status_proses_bg
                }
                val statusTextColor = when (p.status.lowercase(Locale.getDefault())) {
                    "diterima" -> R.color.status_diterima_text
                    "ditolak" -> R.color.status_ditolak_text
                    "review" -> R.color.status_review_text
                    else -> R.color.status_proses_text
                }
                binding.tvDetailStatus.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(requireContext(), statusBgColor)
                binding.tvDetailStatus.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), statusTextColor))

                // PDF Attachment Card Visibility
                if (p.pdfUrl.isNotEmpty()) {
                    binding.tvPdfLabel.visibility = View.VISIBLE
                    binding.btnViewPdf.visibility = View.VISIBLE
                } else {
                    binding.tvPdfLabel.visibility = View.GONE
                    binding.btnViewPdf.visibility = View.GONE
                }
                
                // Video Pitch Attachment Card Visibility and Intent Action
                if (p.pitchVideo.isNotEmpty()) {
                    binding.tvVideoLabel.visibility = View.VISIBLE
                    binding.cvPlayVideo.visibility = View.VISIBLE
                    binding.tvDetailVideoLink.text = p.pitchVideo
                    
                    binding.cvPlayVideo.setOnClickListener {
                        val videoUrl = p.pitchVideo
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Gagal membuka link video", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    binding.tvVideoLabel.visibility = View.GONE
                    binding.cvPlayVideo.visibility = View.GONE
                }
                
                if (p.score > 0 || p.evaluasi.isNotEmpty()) {
                    binding.cardScore.visibility = View.VISIBLE
                    binding.tvDetailScore.text = p.score.toString()
                    binding.tvDetailEvaluasi.text = p.evaluasi
                } else {
                    binding.cardScore.visibility = View.GONE
                }

                // Chat button role routing configuration
                if (role == Constants.ROLE_MAHASISWA) {
                    binding.btnChatInnovator.text = "Hubungi Perusahaan"
                    challengeViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
                        val challenge = challenges.find { it.challengeId == p.challengeId }
                        challenge?.let { ch ->
                            binding.btnChatInnovator.setOnClickListener {
                                val bundle = Bundle().apply {
                                    putString("partnerId", ch.perusahaanId)
                                    putString("partnerName", ch.perusahaanName.ifEmpty { "Perusahaan" })
                                }
                                try {
                                    findNavController().navigate(R.id.navigation_chat_room, bundle)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    challengeViewModel.fetchChallenges()
                } else {
                    binding.btnChatInnovator.text = "Hubungi Inovator"
                    binding.btnChatInnovator.setOnClickListener {
                        val bundle = Bundle().apply {
                            putString("partnerId", p.userId)
                            putString("partnerName", p.userName)
                        }
                        try {
                            findNavController().navigate(R.id.action_proposal_detail_to_chat_room_perusahaan, bundle)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}