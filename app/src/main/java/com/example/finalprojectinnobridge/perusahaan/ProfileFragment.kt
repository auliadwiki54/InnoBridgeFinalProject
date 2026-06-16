package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.FragmentProfileBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.AuthViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId() ?: ""

        // Fetch and observe user data from the ViewModel
        authViewModel.fetchUserData(userId)
        authViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.nama
                binding.tvEmail.text = it.email
                binding.tvRole.text = it.role
            }
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            sessionManager.clearSession()
            findNavController().navigate(R.id.fragment_login)
        }

        binding.btnEditProfile.setOnClickListener {
            // Handle edit profile navigation or logic
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
