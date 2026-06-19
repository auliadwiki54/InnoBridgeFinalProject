package com.example.finalprojectinnobridge.perusahaan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.FragmentEditChallengeBinding
import com.example.finalprojectinnobridge.models.Challenge
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel
import java.util.Calendar

class EditChallengeFragment : Fragment() {

    private var _binding: FragmentEditChallengeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()
    private var challengeId: String? = null

    // SDG yang dipilih
    private val selectedSdgs = mutableSetOf<String>()

    // Map card ID ke label SDG
    private val sdgMap = mapOf(
        R.id.card_sdg_7 to "SDG 7",
        R.id.card_sdg_9 to "SDG 9",
        R.id.card_sdg_11 to "SDG 11",
        R.id.card_sdg_14 to "SDG 14"
    )

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

        setupSdgCards()
        setupDeadlinePicker()
        observeViewModel()

        binding.btnUpdate.setOnClickListener {
            val judul = binding.etJudul.text.toString().trim()
            val deskripsi = binding.etDeskripsi.text.toString().trim()
            val latarBelakang = binding.etLatarBelakang.text.toString().trim()
            val reward = binding.etReward.text.toString().trim()
            val deadline = binding.etDeadline.text.toString().trim()

            if (judul.isEmpty() || deskripsi.isEmpty() || reward.isEmpty() || deadline.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field utama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val existingChallenge = viewModel.challenges.value?.find { it.challengeId == challengeId }
            val kategori = if (selectedSdgs.isNotEmpty()) selectedSdgs.joinToString(", ") else existingChallenge?.kategori ?: ""

            val updatedChallenge = existingChallenge?.copy(
                judul = judul,
                deskripsi = if (latarBelakang.isNotEmpty()) "$deskripsi\n\nLatar Belakang: $latarBelakang" else deskripsi,
                kategori = kategori,
                reward = reward,
                deadline = deadline
            )

            updatedChallenge?.let {
                viewModel.updateChallenge(it) { success, message ->
                    if (success) {
                        Toast.makeText(requireContext(), "Tantangan diperbarui", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), message ?: "Gagal memperbarui", Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: Toast.makeText(requireContext(), "Data tantangan tidak ditemukan", Toast.LENGTH_SHORT).show()
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

    private fun setupSdgCards() {
        sdgMap.forEach { (cardId, sdgLabel) ->
            val card = binding.root.findViewById<com.google.android.material.card.MaterialCardView>(cardId)
            card?.setOnClickListener {
                if (selectedSdgs.contains(sdgLabel)) {
                    selectedSdgs.remove(sdgLabel)
                    card.strokeWidth = 1
                    card.strokeColor = resources.getColor(com.google.android.material.R.color.m3_sys_color_light_outline, null)
                    card.cardElevation = 0f
                } else {
                    selectedSdgs.add(sdgLabel)
                    card.strokeWidth = 3
                    card.strokeColor = resources.getColor(R.color.primary_blue, null)
                    card.cardElevation = 4f
                }
            }
        }
    }

    private fun setupDeadlinePicker() {
        binding.etDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    binding.etDeadline.setText("$day/${month + 1}/$year")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun observeViewModel() {
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val challenge = challenges.find { it.challengeId == challengeId }
            challenge?.let {
                binding.etJudul.setText(it.judul)

                if (it.deskripsi.contains("\n\nLatar Belakang: ")) {
                    val parts = it.deskripsi.split("\n\nLatar Belakang: ")
                    binding.etDeskripsi.setText(parts[0])
                    binding.etLatarBelakang.setText(parts[1])
                } else {
                    binding.etDeskripsi.setText(it.deskripsi)
                }

                binding.etReward.setText(it.reward)
                binding.etDeadline.setText(it.deadline)

                // Restore SDG selection dari kategori yang tersimpan
                selectedSdgs.clear()
                it.kategori.split(", ").forEach { sdg ->
                    val trimmed = sdg.trim()
                    if (trimmed.isNotEmpty()) {
                        selectedSdgs.add(trimmed)
                        // Highlight card yang sesuai
                        sdgMap.entries.find { entry -> entry.value == trimmed }?.let { entry ->
                            val card = binding.root.findViewById<com.google.android.material.card.MaterialCardView>(entry.key)
                            card?.strokeWidth = 3
                            card?.strokeColor = resources.getColor(R.color.primary_blue, null)
                            card?.cardElevation = 4f
                        }
                    }
                }
            }
        }
        viewModel.fetchChallenges()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
