package com.example.finalprojectinnobridge.perusahaan

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.FragmentAddChallengeBinding
import com.example.finalprojectinnobridge.firebase.FirebaseManager
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

    private var selectedSdg: String = ""
    private var selectedImageUri: Uri? = null

    // Launcher untuk mengambil foto dari galeri/kamera
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivChallengePreview.setImageURI(it)
            binding.ivChallengePreview.imageTintList = null // Menghapus tint abu-abu placeholder
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImagePicker()
        setupTargetParticipantChips()
        setupSdgSelection()
        setupDatePicker()

        binding.btnPublish.setOnClickListener {
            validateAndPublish()
        }
    }

    private fun validateAndPublish() {
        val judul = binding.etJudul.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        val latarBelakang = binding.etLatarBelakang.text.toString().trim()
        val deadline = binding.etDeadline.text.toString().trim()
        val reward = binding.etReward.text.toString().trim()

        if (judul.isEmpty()) {
            Toast.makeText(requireContext(), "Judul wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId() ?: ""
        val userName = sessionManager.getUserName() ?: ""

        val selectedChips = mutableListOf<String>()
        for (i in 0 until binding.chipGroupTarget.childCount) {
            val chip = binding.chipGroupTarget.getChildAt(i) as? Chip
            if (chip != null && chip.id != R.id.chip_add_target && chip.isChecked) {
                selectedChips.add(chip.text.toString())
            }
        }
        val targetPeserta = selectedChips.joinToString(", ")

        val checkedRadioId = binding.rgLisensi.checkedRadioButtonId
        val skemaLisensi = if (checkedRadioId != -1) {
            val radioButton = binding.root.findViewById<RadioButton>(checkedRadioId)
            radioButton?.text?.toString() ?: ""
        } else {
            ""
        }

        binding.btnPublish.isEnabled = false
        // Kita butuh ProgressBar di layout, saya asumsikan ada atau tambahkan logic loading
        
        if (selectedImageUri != null) {
            uploadImageAndPublish(userId, userName, judul, deskripsi, latarBelakang, targetPeserta, skemaLisensi, reward, deadline)
        } else {
            publishChallenge("", userId, userName, judul, deskripsi, latarBelakang, targetPeserta, skemaLisensi, reward, deadline)
        }
    }

    private fun uploadImageAndPublish(userId: String, userName: String, judul: String, deskripsi: String, latarBelakang: String, targetPeserta: String, skemaLisensi: String, reward: String, deadline: String) {
        val storageRef = FirebaseManager.getInstance().storage.reference
        val imageRef = storageRef.child("challenges/${userId}_${System.currentTimeMillis()}.jpg")

        selectedImageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        publishChallenge(downloadUri.toString(), userId, userName, judul, deskripsi, latarBelakang, targetPeserta, skemaLisensi, reward, deadline)
                    }
                }
                .addOnFailureListener {
                    binding.btnPublish.isEnabled = true
                    Toast.makeText(requireContext(), "Gagal upload gambar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun publishChallenge(imageUrl: String, userId: String, userName: String, judul: String, deskripsi: String, latarBelakang: String, targetPeserta: String, skemaLisensi: String, reward: String, deadline: String) {
        val challenge = Challenge(
            judul = judul,
            deskripsi = "$deskripsi\n\nLatar Belakang: $latarBelakang",
            targetPeserta = targetPeserta,
            kategori = selectedSdg,
            skemaLisensi = skemaLisensi,
            reward = reward,
            deadline = deadline,
            perusahaanId = userId,
            perusahaanName = userName,
            status = Constants.STATUS_AKTIF,
            imageUrl = imageUrl
        )

        viewModel.addChallenge(challenge) { success, message ->
            binding.btnPublish.isEnabled = true
            if (success) {
                Toast.makeText(requireContext(), "Tantangan berhasil dipublikasikan", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), message ?: "Gagal mempublikasikan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupImagePicker() {
        binding.cardImagePicker.setOnClickListener {
            // Membuka pemilih gambar sistem
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun setupTargetParticipantChips() {
        val childCount = binding.chipGroupTarget.childCount
        val viewsToRemove = mutableListOf<View>()
        for (i in 0 until childCount) {
            val view = binding.chipGroupTarget.getChildAt(i)
            if (view.id != R.id.chip_add_target) {
                viewsToRemove.add(view)
            }
        }
        viewsToRemove.forEach { binding.chipGroupTarget.removeView(it) }

        binding.chipAddTarget.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Tambah Target Peserta Baru")
            val input = EditText(requireContext())
            input.hint = "Contoh: S1 Teknik Informatika"
            builder.setView(input)
            builder.setPositiveButton("Tambah") { dialog, _ ->
                val newTargetText = input.text.toString().trim()
                if (newTargetText.isNotEmpty()) {
                    val newChip = Chip(requireContext()).apply {
                        text = newTargetText
                        isCheckable = true
                        isChecked = true
                        isCloseIconVisible = true
                        setChipBackgroundColorResource(R.color.background_light)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_navy))
                        setOnCloseIconClickListener { binding.chipGroupTarget.removeView(this) }
                    }
                    val currentIndex = binding.chipGroupTarget.childCount - 1
                    binding.chipGroupTarget.addView(newChip, currentIndex)
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }

    private fun setupSdgSelection() {
        val cards = listOf(binding.cardSdg7, binding.cardSdg9, binding.cardSdg11, binding.cardSdg14)
        val sdgLabels = listOf("SDG 7", "SDG 9", "SDG 11", "SDG 14")
        cards.forEachIndexed { index, cardView ->
            cardView.setOnClickListener {
                selectedSdg = sdgLabels[index]
                cards.forEach { card ->
                    card.strokeColor = ContextCompat.getColor(requireContext(), R.color.divider_color)
                    card.strokeWidth = 2
                }
                cardView.strokeColor = ContextCompat.getColor(requireContext(), R.color.primary_blue)
                cardView.strokeWidth = 4
            }
        }
    }

    private fun setupDatePicker() {
        binding.etDeadline.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Batas Akhir Tantangan")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                binding.etDeadline.setText(formatter.format(Date(selection)))
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}