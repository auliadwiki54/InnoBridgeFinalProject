package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.FragmentGiveScoreBinding
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class GiveScoreFragment : Fragment() {

    private var _binding: FragmentGiveScoreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProposalViewModel by viewModels()
    private var proposalId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGiveScoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        proposalId = arguments?.getString("proposalId")
        val judul = arguments?.getString("judul")
        val user = arguments?.getString("userName")

        binding.tvSummaryJudul.text = judul
        binding.tvSummaryUser.text = "Oleh: $user"

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSubmitScore.isEnabled = !isLoading
        }

        binding.btnSubmitScore.setOnClickListener {
            val scoreStr = binding.etScore.text.toString().trim()
            val evaluasi = binding.etEvaluasi.text.toString().trim()
            
            val status = when (binding.rgStatus.checkedRadioButtonId) {
                R.id.rb_acc -> "Diterima"
                R.id.rb_tolak -> "Ditolak"
                else -> "Pending"
            }

            if (scoreStr.isEmpty()) {
                Toast.makeText(requireContext(), "Harap masukkan skor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val score = scoreStr.toIntOrNull() ?: 0
            if (score < 0 || score > 100) {
                Toast.makeText(requireContext(), "Skor harus antara 0-100", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            proposalId?.let { id ->
                viewModel.updateScoreAndEvaluation(id, score, evaluasi, status) { success, error ->
                    if (success) {
                        Toast.makeText(requireContext(), "Evaluasi berhasil disimpan", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), error ?: "Gagal menyimpan evaluasi", Toast.LENGTH_SHORT).show()
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