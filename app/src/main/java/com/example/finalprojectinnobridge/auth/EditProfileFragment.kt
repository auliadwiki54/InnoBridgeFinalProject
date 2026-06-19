package com.example.finalprojectinnobridge.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.databinding.FragmentEditProfileBinding
import com.example.finalprojectinnobridge.models.User
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.AuthViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId() ?: ""

        authViewModel.fetchUserData(userId)
        authViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                currentUser = it
                binding.etName.setText(it.nama)
                binding.etBio.setText(it.bio)
                // Tampilkan email saat ini sebagai hint
                binding.tilEmail.hint = "Email Baru (saat ini: ${it.email})"

                // Toggle visibility and bind dynamic fields based on role
                if (it.role.equals("Mahasiswa", ignoreCase = true)) {
                    binding.llMahasiswaFields.visibility = View.VISIBLE
                    binding.llPerusahaanFields.visibility = View.GONE
                    binding.etUniversitas.setText(it.universitas)
                    binding.etJurusan.setText(it.jurusan)
                    binding.etKeahlian.setText(it.keahlian)
                } else if (it.role.equals("Perusahaan", ignoreCase = true)) {
                    binding.llMahasiswaFields.visibility = View.GONE
                    binding.llPerusahaanFields.visibility = View.VISIBLE
                    binding.etIndustri.setText(it.industri)
                    binding.etWebsite.setText(it.website)
                    binding.etAlamat.setText(it.alamat)
                }
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveProfile.isEnabled = !isLoading
        }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val bio = binding.etBio.text.toString().trim()
            val newEmail = binding.etEmail.text.toString().trim()
            val newPassword = binding.etPassword.text.toString().trim()

            // Validasi nama
            if (name.isEmpty()) {
                binding.tilName.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }
            binding.tilName.error = null

            // Validasi password jika diisi
            if (newPassword.isNotEmpty() && newPassword.length < 6) {
                binding.tilPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }
            binding.tilPassword.error = null

            val user = currentUser ?: return@setOnClickListener
            val updatedUser = user.copy(
                nama = name,
                bio = bio,
                email = if (newEmail.isNotEmpty()) newEmail else user.email,
                universitas = binding.etUniversitas.text.toString().trim(),
                jurusan = binding.etJurusan.text.toString().trim(),
                keahlian = binding.etKeahlian.text.toString().trim(),
                industri = binding.etIndustri.text.toString().trim(),
                alamat = binding.etAlamat.text.toString().trim(),
                website = binding.etWebsite.text.toString().trim()
            )

            // Simpan data profil ke Firestore dulu
            authViewModel.updateUser(updatedUser) { success, error ->
                if (!success) {
                    Toast.makeText(requireContext(), error ?: "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                    return@updateUser
                }

                sessionManager.updateUserInfo(updatedUser.nama, updatedUser.email)

                // Update email di Firebase Auth jika diubah
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (newEmail.isNotEmpty() && newEmail != user.email) {
                    firebaseUser?.updateEmail(newEmail)
                        ?.addOnSuccessListener {
                            updatePasswordIfNeeded(firebaseUser, newPassword)
                        }
                        ?.addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Profil disimpan, tapi email gagal diperbarui: ${e.message}", Toast.LENGTH_LONG).show()
                            findNavController().navigateUp()
                        }
                } else {
                    updatePasswordIfNeeded(firebaseUser, newPassword)
                }
            }
        }
    }

    private fun updatePasswordIfNeeded(firebaseUser: com.google.firebase.auth.FirebaseUser?, newPassword: String) {
        if (newPassword.isNotEmpty()) {
            firebaseUser?.updatePassword(newPassword)
                ?.addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profil & password diperbarui", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                ?.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Profil disimpan, tapi password gagal: ${e.message}", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
        } else {
            Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
