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
import com.example.finalprojectinnobridge.activities.MainActivity
import com.example.finalprojectinnobridge.databinding.FragmentLoginBinding
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.utils.Validator
import com.example.finalprojectinnobridge.viewmodels.AuthViewModel

class LogInFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val role = if (binding.toggleRole.checkedButtonId == R.id.btn_role_mahasiswa) 
                Constants.ROLE_MAHASISWA else Constants.ROLE_PERUSAHAAN

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

            viewModel.login(email, password) { success, message ->
                if (success) {
                    val user = viewModel.user.value
                    if (user != null) {
                        if (user.role == role) {
                            SessionManager(requireContext()).saveSession(user.uid, user.role)
                            (activity as? MainActivity)?.updateBottomNavigation()
                            navigateToDashboard(user.role)
                        } else {
                            Toast.makeText(requireContext(), "Role tidak sesuai", Toast.LENGTH_SHORT).show()
                            viewModel.logout()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), message ?: "Login Gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToDashboard(role: String) {
        if (role == Constants.ROLE_MAHASISWA) {
            findNavController().navigate(R.id.action_login_to_home)
        } else {
            findNavController().navigate(R.id.action_login_to_dashboard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}