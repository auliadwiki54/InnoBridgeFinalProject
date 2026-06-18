package com.example.finalprojectinnobridge.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.activities.MainActivity
import com.example.finalprojectinnobridge.databinding.FragmentSplashBinding
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.utils.SimulasiData

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animasikan logo: Fade In + Scale Up
        val animation = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
        binding.ivLogo.startAnimation(animation)
        
        // Jalankan Simulasi Data Tantangan (Seeder)
        SimulasiData.generateSimulasiTantangan()

        Handler(Looper.getMainLooper()).postDelayed({
            val sessionManager = SessionManager(requireContext())
            if (sessionManager.isLoggedIn()) {
                val myId = sessionManager.getUserId() ?: ""
                val role = sessionManager.getUserRole()
                
                // Jalankan Simulasi Chat agar inbox terasa hidup
                if (myId.isNotEmpty()) {
                    SimulasiData.generateSimulasiChat(myId)
                }

                (activity as? MainActivity)?.updateBottomNavigation()
                if (role == Constants.ROLE_MAHASISWA) {
                    findNavController().navigate(R.id.navigation_home)
                } else {
                    findNavController().navigate(R.id.navigation_dashboard)
                }
            } else {
                findNavController().navigate(R.id.action_splash_to_login)
            }
        }, 2500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}