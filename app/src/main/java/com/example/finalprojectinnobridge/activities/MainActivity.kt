package com.example.finalprojectinnobridge.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.ActivityMainBinding
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        updateBottomNavigation()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val sessionManager = SessionManager(this)
            val role = sessionManager.getUserRole()

            val mahasiswaDestinations = setOf(
                R.id.navigation_home,
                R.id.navigation_chat,
                R.id.navigation_challenge,
                R.id.navigation_my_proposal,
                R.id.navigation_profile
            )

            // FIX: Tambahkan navigation_chat_perusahaan agar navbar tetap muncul
            val perusahaanDestinations = setOf(
                R.id.navigation_dashboard,
                R.id.navigation_chat_perusahaan,
                R.id.navigation_add_challenge,
                R.id.navigation_proposal_list,
                R.id.navigation_profile_perusahaan
            )

            val shouldShow = when (role) {
                Constants.ROLE_MAHASISWA -> destination.id in mahasiswaDestinations
                Constants.ROLE_PERUSAHAAN -> destination.id in perusahaanDestinations
                else -> false
            }

            binding.bottomNavigation?.visibility = if (shouldShow) View.VISIBLE else View.GONE
            supportActionBar?.setDisplayShowTitleEnabled(!shouldShow)
        }

        binding.bottomNavigation?.setOnItemSelectedListener { item ->
            try {
                navController.navigate(item.itemId)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun updateBottomNavigation() {
        val sessionManager = SessionManager(this)
        val role = sessionManager.getUserRole()

        binding.bottomNavigation?.menu?.clear()

        if (role == Constants.ROLE_MAHASISWA) {
            binding.bottomNavigation?.inflateMenu(R.menu.bottom_nav_menu)
            appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_home, R.id.navigation_chat, R.id.navigation_challenge, R.id.navigation_my_proposal, R.id.navigation_profile)
            )
        } else if (role == Constants.ROLE_PERUSAHAAN) {
            binding.bottomNavigation?.inflateMenu(R.menu.bottom_nav_menu_perusahaan)
            appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_dashboard, R.id.navigation_chat_perusahaan, R.id.navigation_add_challenge, R.id.navigation_proposal_list, R.id.navigation_profile_perusahaan)
            )
        } else {
            appBarConfiguration = AppBarConfiguration(setOf(R.id.fragment_login, R.id.fragment_splash))
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}