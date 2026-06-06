package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.databinding.FragmentEditChallengeBinding
import com.example.finalprojectinnobridge.models.Challenge
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel

class EditChallengeFragment : Fragment() {

    private var _binding: FragmentEditChallengeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()
    private var challengeId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challengeId = arguments?.getString("challengeId")

        observeViewModel()

        binding.btnUpdate.setOnClickListener {
            val judul = binding.etJudul.text.toString().trim()
            val deskripsi = binding.etDeskripsi.text.toString().trim()
            val kategori = binding.etKategori.text.toString().trim()
            val reward = binding.etReward.text.toString().trim()
            val deadline = binding.etDeadline.text.toString().trim()

            if (judul.isEmpty() || deskripsi.isEmpty() || kategori.isEmpty() || reward.isEmpty() || deadline.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val existingChallenge = viewModel.challenges.value?.find { it.challengeId == challengeId }
            val updatedChallenge = existingChallenge?.copy(
                judul = judul,
                deskripsi = deskripsi,
                kategori = kategori,
                reward = reward,
                deadline = deadline
            )

            updatedChallenge?.let {
                viewModel.addChallenge(it) { success, message ->
                    if (success) {
                        Toast.makeText(requireContext(), "Tantangan diperbarui", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), message ?: "Gagal memperbarui", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnDelete.setOnClickListener {
            challengeId?.let { id ->
                viewModel.deleteChallenge(id) { success, message ->
                    if (success) {
                        Toast.makeText(requireContext(), "Tantangan dihapus", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), message ?: "Gagal menghapus", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val challenge = challenges.find { it.challengeId == challengeId }
            challenge?.let {
                binding.etJudul.setText(it.judul)
                binding.etDeskripsi.setText(it.deskripsi)
                binding.etKategori.setText(it.kategori)
                binding.etReward.setText(it.reward)
                binding.etDeadline.setText(it.deadline)
            }
        }
        viewModel.fetchChallenges()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}