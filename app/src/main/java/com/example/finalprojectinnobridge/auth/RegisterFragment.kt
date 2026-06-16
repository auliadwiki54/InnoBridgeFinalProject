package com.example.finalprojectinnobridge.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.FragmentRegisterBinding
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.Validator
import com.example.finalprojectinnobridge.viewmodels.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    // Default role saat halaman pertama kali dibuka
    private var selectedRole: String = Constants.ROLE_MAHASISWA

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup awal visual Card Role (Mahasiswa terpilih secara default)
        updateRoleUI()

        // Listener saat Card Mahasiswa diklik
        binding.cardMahasiswa.setOnClickListener {
            selectedRole = Constants.ROLE_MAHASISWA
            updateRoleUI()
        }

        // Listener saat Card Perusahaan diklik
        binding.cardPerusahaan.setOnClickListener {
            selectedRole = Constants.ROLE_PERUSAHAAN
            updateRoleUI()
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etNama.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.lengkapi_data), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Validator.validateEmail(email)) {
                binding.tilEmail.error = "Email tidak valid"
                return@setOnClickListener
            }
            binding.tilEmail.error = null

            if (!Validator.validatePassword(password)) {
                binding.tilPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }
            binding.tilPassword.error = null

            if (password != confirmPassword) {
                binding.tilConfirmPassword.error = "Password tidak cocok"
                return@setOnClickListener
            }
            binding.tilConfirmPassword.error = null

            // Tampilkan progress bar jika diperlukan saat proses registrasi ke server/Firebase
            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false

            viewModel.register(name, email, password, selectedRole) { success, message ->
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true

                if (success) {
                    Toast.makeText(requireContext(), "Registrasi Berhasil", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), message ?: "Registrasi Gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Fungsi helper untuk mengubah warna border, background, teks, dan ikon
     * secara dinamis di Kotlin sesuai dengan role yang dipilih user.
     */
    private fun updateRoleUI() {
        val context = requireContext()

        if (selectedRole == Constants.ROLE_MAHASISWA) {
            // Aktifkan visual Mahasiswa
            binding.cardMahasiswa.setCardBackgroundColor(ContextCompat.getColor(context, R.color.secondary_blue))
            binding.cardMahasiswa.strokeColor = ContextCompat.getColor(context, R.color.primary_blue)
            binding.cardMahasiswa.strokeWidth = 4 // dalam satuan pixel, bisa disesuaikan
            binding.ivMahasiswa.setColorFilter(ContextCompat.getColor(context, R.color.primary_blue))
            binding.tvMahasiswa.setTextColor(ContextCompat.getColor(context, R.color.primary_blue))

            // Matikan visual Perusahaan
            binding.cardPerusahaan.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            binding.cardPerusahaan.strokeColor = ContextCompat.getColor(context, R.color.divider_color)
            binding.cardPerusahaan.strokeWidth = 2
            binding.ivPerusahaan.setColorFilter(ContextCompat.getColor(context, R.color.text_light_grey))
            binding.tvPerusahaan.setTextColor(ContextCompat.getColor(context, R.color.text_light_grey))
        } else {
            // Aktifkan visual Perusahaan
            binding.cardPerusahaan.setCardBackgroundColor(ContextCompat.getColor(context, R.color.secondary_blue))
            binding.cardPerusahaan.strokeColor = ContextCompat.getColor(context, R.color.primary_blue)
            binding.cardPerusahaan.strokeWidth = 4
            binding.ivPerusahaan.setColorFilter(ContextCompat.getColor(context, R.color.primary_blue))
            binding.tvPerusahaan.setTextColor(ContextCompat.getColor(context, R.color.primary_blue))

            // Matikan visual Mahasiswa
            binding.cardMahasiswa.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            binding.cardMahasiswa.strokeColor = ContextCompat.getColor(context, R.color.divider_color)
            binding.cardMahasiswa.strokeWidth = 2
            binding.ivMahasiswa.setColorFilter(ContextCompat.getColor(context, R.color.text_light_grey))
            binding.tvMahasiswa.setTextColor(ContextCompat.getColor(context, R.color.text_light_grey))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}