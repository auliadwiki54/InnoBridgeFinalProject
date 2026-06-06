package com.example.finalprojectinnobridge.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.databinding.FragmentSubmitProposalBinding
import com.example.finalprojectinnobridge.models.Proposal
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class SubmitProposalFragment : Fragment() {

    private var _binding: FragmentSubmitProposalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProposalViewModel by viewModels()
    private var challengeId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubmitProposalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challengeId = arguments?.getString("challengeId")

        binding.btnSubmit.setOnClickListener {
            val judul = binding.etJudul.text.toString().trim()
            val solusi = binding.etSolusi.text.toString().trim()

            if (judul.isEmpty() || solusi.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = SessionManager(requireContext()).getUserId() ?: ""
            val proposal = Proposal(
                challengeId = challengeId ?: "",
                userId = userId,
                judul = judul,
                solusi = solusi,
                status = "Pending"
            )

            viewModel.submitProposal(proposal) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "Proposal berhasil dikirim", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), message ?: "Gagal mengirim proposal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}