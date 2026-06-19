package com.example.finalprojectinnobridge.mahasiswa

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.databinding.FragmentSubmitProposalBinding
import com.example.finalprojectinnobridge.firebase.FirebaseManager
import com.example.finalprojectinnobridge.models.Proposal
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class SubmitProposalFragment : Fragment() {

    private var _binding: FragmentSubmitProposalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProposalViewModel by viewModels()
    private var challengeId: String? = null
    private var selectedPdfUri: Uri? = null

    private val pdfPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPdfUri = result.data?.data
            selectedPdfUri?.let { uri ->
                val fileName = getFileName(uri)
                binding.tvPdfName.text = fileName
                binding.tvPdfName.visibility = View.VISIBLE
            }
        }
    }

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

        binding.btnSelectPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pdfPickerLauncher.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            val judul = binding.etJudul.text.toString().trim()
            val solusi = binding.etSolusi.text.toString().trim()
            val videoUrl = binding.etVideoUrl.text.toString().trim()
            val proposalLink = binding.etProposalLink.text.toString().trim()

            if (judul.isEmpty() || solusi.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi judul dan solusi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPdfUri == null && proposalLink.isEmpty()) {
                Toast.makeText(requireContext(), "Harap pilih dokumen PDF atau masukkan link proposal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!binding.switchHki.isChecked) {
                Toast.makeText(requireContext(), "Harap setujui syarat HKI", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPdfUri != null) {
                uploadPdfAndSubmit(judul, solusi, videoUrl, proposalLink)
            } else {
                submitProposalWithoutPdf(judul, solusi, videoUrl, proposalLink)
            }
        }
    }

    private fun uploadPdfAndSubmit(judul: String, solusi: String, videoUrl: String, proposalLink: String) {
        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId() ?: ""
        val userName = sessionManager.getUserName() ?: ""
        
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        val storageRef = FirebaseManager.getInstance().storage.reference
        val pdfRef = storageRef.child("proposals/${userId}_${System.currentTimeMillis()}.pdf")

        selectedPdfUri?.let { uri ->
            pdfRef.putFile(uri)
                .addOnSuccessListener {
                    pdfRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val proposal = Proposal(
                            challengeId = challengeId ?: "",
                            userId = userId,
                            userName = userName,
                            userUniversity = "Universitas Inovasi", // Placeholder
                            judul = judul,
                            solusi = solusi,
                            pitchVideo = videoUrl,
                            pdfUrl = downloadUri.toString(),
                            status = "Pending"
                        )

                        viewModel.submitProposal(proposal) { success, message ->
                            binding.progressBar.visibility = View.GONE
                            binding.btnSubmit.isEnabled = true
                            if (success) {
                                Toast.makeText(requireContext(), "Proposal berhasil dikirim", Toast.LENGTH_SHORT).show()
                                findNavController().navigateUp()
                            } else {
                                Toast.makeText(requireContext(), message ?: "Gagal mengirim proposal", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(requireContext(), "Gagal upload PDF: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun submitProposalWithoutPdf(judul: String, solusi: String, videoUrl: String, proposalLink: String) {
        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId() ?: ""
        val userName = sessionManager.getUserName() ?: ""
        
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        val proposal = Proposal(
            challengeId = challengeId ?: "",
            userId = userId,
            userName = userName,
            userUniversity = "Universitas Inovasi", // Placeholder
            judul = judul,
            solusi = solusi,
            pitchVideo = videoUrl,
            pdfUrl = proposalLink,
            status = "Pending"
        )

        viewModel.submitProposal(proposal) { success, message ->
            binding.progressBar.visibility = View.GONE
            binding.btnSubmit.isEnabled = true
            if (success) {
                Toast.makeText(requireContext(), "Proposal berhasil dikirim", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), message ?: "Gagal mengirim proposal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "file.pdf"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}