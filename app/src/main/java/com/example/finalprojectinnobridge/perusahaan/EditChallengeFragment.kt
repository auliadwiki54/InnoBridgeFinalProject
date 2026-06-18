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

        // Mengambil argument ID tantangan yang dikirim dari list dashboard
        challengeId = arguments?.getString("challengeId")

        observeViewModel()

        // Tombol Update Tantangan
        binding.btnUpdate.setOnClickListener {
            val judul = binding.etJudul.text.toString().trim()
            val deskripsi = binding.etDeskripsi.text.toString().trim()
            val latarBelakang = binding.etLatarBelakang.text.toString().trim()
            val reward = binding.etReward.text.toString().trim()
            val deadline = binding.etDeadline.text.toString().trim()

            // Validasi Input Utama
            if (judul.isEmpty() || deskripsi.isEmpty() || reward.isEmpty() || deadline.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field utama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val existingChallenge = viewModel.challenges.value?.find { it.challengeId == challengeId }

            // Mengemas data lama dan memperbarui field yang diubah
            val updatedChallenge = existingChallenge?.copy(
                judul = judul,
                deskripsi = "$deskripsi\n\nLatar Belakang: $latarBelakang",
                kategori = existingChallenge.kategori, // Tetap memegang kategori awal agar tidak null
                reward = reward,
                deadline = deadline
            )

            updatedChallenge?.let {
                // 🌟 KUNCI FIX: Panggil fungsi UPDATE, jangan fungsi ADD agar tidak menduplikat data baru
                viewModel.updateChallenge(it) { success, message ->
                    if (success) {
                        Toast.makeText(requireContext(), "Tantangan diperbarui", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), message ?: "Gagal memperbarui", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Tombol Hapus Tantangan
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

                // Memisahkan kembali string deskripsi dan latar belakang secara cerdas jika mengandung penanda pemisah
                if (it.deskripsi.contains("\n\nLatar Belakang: ")) {
                    val parts = it.deskripsi.split("\n\nLatar Belakang: ")
                    binding.etDeskripsi.setText(parts[0])
                    binding.etLatarBelakang.setText(parts[1])
                } else {
                    binding.etDeskripsi.setText(it.deskripsi)
                }

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