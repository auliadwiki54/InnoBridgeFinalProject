package com.example.finalprojectinnobridge.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etNama.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val role = if (binding.toggleRole.checkedButtonId == R.id.btn_role_mahasiswa)
                Constants.ROLE_MAHASISWA else Constants.ROLE_PERUSAHAAN

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

            viewModel.register(name, email, password, role) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "Registrasi Berhasil", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), message ?: "Registrasi Gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
