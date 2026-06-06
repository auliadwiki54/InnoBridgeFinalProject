package com.example.finalprojectinnobridge.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.activities.MainActivity
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.SessionManager

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            val sessionManager = SessionManager(requireContext())
            if (sessionManager.isLoggedIn()) {
                val role = sessionManager.getUserRole()
                (activity as? MainActivity)?.updateBottomNavigation()
                if (role == Constants.ROLE_MAHASISWA) {
                    findNavController().navigate(R.id.action_login_to_home)
                } else {
                    findNavController().navigate(R.id.action_login_to_dashboard)
                }
            } else {
                findNavController().navigate(R.id.action_splash_to_login)
            }
        }, 2000)
    }
}