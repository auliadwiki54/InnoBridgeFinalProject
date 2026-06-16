package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddChallengeFragment : Fragment() {

    private var _binding: FragmentAddChallengeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()

    // Variabel untuk menampung target SDG yang dipilih oleh perusahaan
    private var selectedSdg: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi fitur klik tambahan
        setupSdgSelection()
        setupDatePicker()

        binding.btnPublish.setOnClickListener {
            val judul = binding.etJudul.text.toString().trim()
            val deskripsi = binding.etDeskripsi.text.toString().trim()
            val latarBelakang = binding.etLatarBelakang.text.toString().trim()
            val deadline = binding.etDeadline.text.toString().trim()

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

            // Validasi Input secara menyeluruh termasuk SDG dan Deadline
            if (judul.isEmpty() || deskripsi.isEmpty() || latarBelakang.isEmpty() ||
                targetPeserta.isEmpty() || selectedSdg.isEmpty() || skemaLisensi.isEmpty() || deadline.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field, pilih target SDG, dan tanggal deadline", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = SessionManager(requireContext()).getUserId() ?: ""

            // Membuat objek Challenge baru yang membawa data SDG dan Deadline asli
            val challenge = Challenge(
                judul = judul,
                deskripsi = "$deskripsi\n\nLatar Belakang: $latarBelakang",
                targetPeserta = targetPeserta,
                kategori = selectedSdg, // Otomatis terisi string seperti "SDG 7", "SDG 9", dll.
                skemaLisensi = skemaLisensi,
                reward = "Sesuai Skema",
                deadline = deadline, // Tanggal hasil pick kalender (contoh: "25 Juni 2026")
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

    /**
     * Mengatur sistem pemilihan grid target SDGs secara interaktif
     */
    private fun setupSdgSelection() {
        val cards = listOf(binding.cardSdg7, binding.cardSdg9, binding.cardSdg11, binding.cardSdg14)
        val sdgLabels = listOf("SDG 7", "SDG 9", "SDG 11", "SDG 14")

        cards.forEachIndexed { index, cardView ->
            cardView.setOnClickListener {
                selectedSdg = sdgLabels[index]

                // Kembalikan semua warna border kartu ke default (abu-abu terang)
                cards.forEach { card ->
                    card.strokeColor = ContextCompat.getColor(requireContext(), R.color.divider_color)
                    card.strokeWidth = 2 // ketebalan 1dp dalam satuan pixel
                }

                // Highlight border kartu yang baru saja diklik menjadi biru utama aplikasi
                cardView.strokeColor = ContextCompat.getColor(requireContext(), R.color.primary_blue)
                cardView.strokeWidth = 4 // Tebalkan border agar perubahan terlihat jelas

                Toast.makeText(requireContext(), "$selectedSdg Terpilih", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Membuka Dialog Kalender Material secara Native saat field deadline disentuh
     */
    private fun setupDatePicker() {
        binding.etDeadline.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Batas Akhir Tantangan")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                // Format tanggal Indonesia: dd MMMM yyyy (Contoh: 16 Juni 2026)
                val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val calendarDate = Date(selection)
                binding.etDeadline.setText(formatter.format(calendarDate))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}