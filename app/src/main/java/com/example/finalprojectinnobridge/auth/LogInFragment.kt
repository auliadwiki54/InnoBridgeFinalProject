package com.example.finalprojectinnobridge.auth

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.activities.MainActivity
import com.example.finalprojectinnobridge.databinding.FragmentLoginBinding
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LogInFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager
    private var selectedRole: String = Constants.ROLE_MAHASISWA

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        credentialManager = CredentialManager.create(requireContext())

        setupRoleSelection()

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.btnLogin.setOnClickListener {
            handleEmailLogin()
        }

        binding.btnGoogle.setOnClickListener {
            signInWithGoogle(selectedRole)
        }
    }

    private fun setupRoleSelection() {
        binding.cardMahasiswa.setOnClickListener {
            selectRole(Constants.ROLE_MAHASISWA)
        }
        binding.cardPerusahaan.setOnClickListener {
            selectRole(Constants.ROLE_PERUSAHAAN)
        }
        // Set default
        selectRole(Constants.ROLE_MAHASISWA)
    }

    private fun selectRole(role: String) {
        selectedRole = role
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary_blue)
        val greyColor = ContextCompat.getColor(requireContext(), R.color.text_light_grey)
        val bgColor = ContextCompat.getColor(requireContext(), R.color.secondary_blue)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val dividerColor = ContextCompat.getColor(requireContext(), R.color.divider_color)

        if (role == Constants.ROLE_MAHASISWA) {
            // Selected Mahasiswa
            binding.cardMahasiswa.apply {
                setCardBackgroundColor(bgColor)
                strokeColor = primaryColor
                strokeWidth = 4
            }
            binding.ivMahasiswa.imageTintList = ColorStateList.valueOf(primaryColor)
            binding.tvMahasiswa.setTextColor(primaryColor)

            // Deselected Perusahaan
            binding.cardPerusahaan.apply {
                setCardBackgroundColor(whiteColor)
                strokeColor = dividerColor
                strokeWidth = 2
            }
            binding.ivPerusahaan.imageTintList = ColorStateList.valueOf(greyColor)
            binding.tvPerusahaan.setTextColor(greyColor)
        } else {
            // Selected Perusahaan
            binding.cardPerusahaan.apply {
                setCardBackgroundColor(bgColor)
                strokeColor = primaryColor
                strokeWidth = 4
            }
            binding.ivPerusahaan.imageTintList = ColorStateList.valueOf(primaryColor)
            binding.tvPerusahaan.setTextColor(primaryColor)

            // Deselected Mahasiswa
            binding.cardMahasiswa.apply {
                setCardBackgroundColor(whiteColor)
                strokeColor = dividerColor
                strokeWidth = 2
            }
            binding.ivMahasiswa.imageTintList = ColorStateList.valueOf(greyColor)
            binding.tvMahasiswa.setTextColor(greyColor)
        }
    }

    private fun handleEmailLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi email dan password", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        viewModel.login(email, password) { success, message ->
            binding.progressBar.visibility = View.GONE
            if (success) {
                val user = viewModel.user.value
                if (user != null) {
                    if (user.role == selectedRole) {
                        // 🌟 FIX: Memakai user.nama sesuai rancangan model data class User Anda
                        SessionManager(requireContext()).saveSession(
                            userId = user.uid,
                            role = user.role,
                            name = if (user.nama.isBlank()) "Ino Sinom" else user.nama,
                            email = if (user.email.isBlank()) email else user.email
                        )
                        (activity as? MainActivity)?.updateBottomNavigation()
                        navigateToDashboard(user.role)
                    } else {
                        Toast.makeText(requireContext(), "Role tidak sesuai dengan akun ini", Toast.LENGTH_SHORT).show()
                        viewModel.logout()
                    }
                }
            } else {
                Toast.makeText(requireContext(), message ?: "Login Gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle(selectedRole: String) {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                binding.btnGoogle.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE

                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext(),
                )

                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    val idToken = credential.idToken
                    viewModel.signInWithGoogle(idToken, selectedRole) { success, message, role ->
                        binding.btnGoogle.isEnabled = true
                        binding.progressBar.visibility = View.GONE

                        if (success && role != null) {
                            val user = viewModel.user.value
                            if (user != null) {
                                // 🌟 FIX: Sinkronisasi pemanggilan Google Sign-In menggunakan tipe String murni
                                val googleName = credential.displayName ?: "Ino Sinom"
                                val googleEmail = credential.id

                                SessionManager(requireContext()).saveSession(
                                    userId = user.uid,
                                    role = role,
                                    name = if (user.nama.isBlank()) googleName else user.nama,
                                    email = if (user.email.isBlank()) googleEmail else user.email
                                )
                                (activity as? MainActivity)?.updateBottomNavigation()
                                navigateToDashboard(role)
                            }
                        } else {
                            Toast.makeText(requireContext(), message ?: "Login Gagal", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                binding.btnGoogle.isEnabled = true
                binding.progressBar.visibility = View.GONE
                Log.e("LogInFragment", "Google Sign-In failed", e)
                Toast.makeText(requireContext(), "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
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