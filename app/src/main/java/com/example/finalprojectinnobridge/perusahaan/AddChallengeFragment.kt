package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.databinding.FragmentAddChallengeBinding
import com.example.finalprojectinnobridge.models.Challenge
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel

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
            val targetPeserta = binding.etTargetPeserta.text.toString().trim()
            val kategori = binding.etKategori.text.toString().trim()
            val skemaLisensi = binding.etSkemaLisensi.text.toString().trim()
            val reward = binding.etReward.text.toString().trim()
            val deadline = binding.etDeadline.text.toString().trim()

            if (judul.isEmpty() || deskripsi.isEmpty() || targetPeserta.isEmpty() || 
                kategori.isEmpty() || skemaLisensi.isEmpty() || reward.isEmpty() || deadline.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = SessionManager(requireContext()).getUserId() ?: ""
            val challenge = Challenge(
                judul = judul,
                deskripsi = deskripsi,
                targetPeserta = targetPeserta,
                kategori = kategori,
                skemaLisensi = skemaLisensi,
                reward = reward,
                deadline = deadline,
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