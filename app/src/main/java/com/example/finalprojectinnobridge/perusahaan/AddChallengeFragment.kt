package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.FragmentAddChallengeBinding
import com.example.finalprojectinnobridge.models.Challenge
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel
import com.google.android.material.chip.Chip

class AddChallengeFragment : Fragment() {

    private var _binding: FragmentAddChallengeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPublish.setOnClickListener {
            val judul = binding.etJudul.text.toString().trim()
            val deskripsi = binding.etDeskripsi.text.toString().trim()
            val latarBelakang = binding.etLatarBelakang.text.toString().trim()

            // 1. Mengambil data dari ChipGroup (Target Peserta) yang aktif/ada
            val selectedChips = mutableListOf<String>()
            for (i in 0 until binding.chipGroupTarget.childCount) {
                val chip = binding.chipGroupTarget.getChildAt(i) as? Chip
                // Lewati chip "+" jika itu hanya tombol penambah
                if (chip != null && chip.text != "+") {
                    selectedChips.add(chip.text.toString())
                }
            }
            val targetPeserta = selectedChips.joinToString(", ")

            // 2. Mengambil data dari RadioGroup (Skema Lisensi)
            val selectedLicenseId = binding.rgLisensi.checkedRadioButtonId
            val skemaLisensi = if (selectedLicenseId != -1) {
                val radioButton = binding.root.findViewById<RadioButton>(selectedLicenseId)
                radioButton.text.toString()
            } else {
                ""
            }

            // Validasi Input
            if (judul.isEmpty() || deskripsi.isEmpty() || latarBelakang.isEmpty() ||
                targetPeserta.isEmpty() || skemaLisensi.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field dan pilih lisensi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = SessionManager(requireContext()).getUserId() ?: ""

            // Membuat objek Challenge baru yang disesuaikan dengan field XML baru Anda
            val challenge = Challenge(
                judul = judul,
                deskripsi = "$deskripsi\n\nLatar Belakang: $latarBelakang", // Menggabungkan deskripsi & latar belakang
                targetPeserta = targetPeserta,
                kategori = "SDGs Terkait", // Bisa disesuaikan nanti dari grid yang dipilih
                skemaLisensi = skemaLisensi,
                reward = "Sesuai Skema", // Sesuaikan dengan kebutuhan model data Anda
                deadline = "-",
                perusahaanId = userId,
                status = Constants.STATUS_AKTIF
            )

            viewModel.addChallenge(challenge) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "Tantangan berhasil dipublikasikan", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), message ?: "Gagal mempublikasikan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}