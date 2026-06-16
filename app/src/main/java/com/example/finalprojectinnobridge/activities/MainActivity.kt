package com.example.finalprojectinnobridge.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
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

            val isMahasiswa = role == Constants.ROLE_MAHASISWA
            val isPerusahaan = role == Constants.ROLE_PERUSAHAAN

            // 4 Destinasi menu utama dasar masing-masing peran
            val mahasiswaDestinations = setOf(
                R.id.navigation_home, R.id.navigation_chat, R.id.navigation_my_proposal, R.id.navigation_profile
            )
            val perusahaanDestinations = setOf(
                R.id.navigation_dashboard, R.id.navigation_chat, R.id.navigation_proposal_list, R.id.navigation_profile_perusahaan
            )

            // FAB tetap tampil saat Mahasiswa berada di halaman utama ataupun halaman list tantangan
            if (isMahasiswa && (destination.id in mahasiswaDestinations || destination.id == R.id.navigation_challenge)) {
                binding.bottomAppBar.visibility = View.VISIBLE
                binding.fabAdd.visibility = View.VISIBLE
                supportActionBar?.setDisplayShowTitleEnabled(false)
            } else if (isPerusahaan && destination.id in perusahaanDestinations) {
                binding.bottomAppBar.visibility = View.VISIBLE
                binding.fabAdd.visibility = View.VISIBLE
                supportActionBar?.setDisplayShowTitleEnabled(false)
            } else {
                binding.bottomAppBar.visibility = View.GONE
                binding.fabAdd.visibility = View.GONE
                supportActionBar?.setDisplayShowTitleEnabled(true)
            }
        }

        binding.fabAdd.setOnClickListener {
            val sessionManager = SessionManager(this)
            val role = sessionManager.getUserRole()

            when (role) {
                Constants.ROLE_MAHASISWA -> {
                    // Tombol + Mahasiswa membuka halaman list Tantangan (ChallengeFragment)
                    navController.navigate(R.id.navigation_challenge)
                }
                Constants.ROLE_PERUSAHAAN -> {
                    // Tombol + Perusahaan membuka halaman form Add Challenge
                    navController.navigate(R.id.navigation_add_challenge)
                }
                else -> {
                    Toast.makeText(this, "Akses peran tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun updateBottomNavigation() {
        val sessionManager = SessionManager(this)
        val role = sessionManager.getUserRole()

        binding.bottomNavigation.menu.clear()
        if (role == Constants.ROLE_MAHASISWA) {
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
            val topLevelDestinations = setOf(
                R.id.navigation_home, R.id.navigation_chat, R.id.navigation_my_proposal, R.id.navigation_profile
            )
            appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        } else if (role == Constants.ROLE_PERUSAHAAN) {
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_perusahaan)
            val topLevelDestinations = setOf(
                R.id.navigation_dashboard, R.id.navigation_chat, R.id.navigation_proposal_list, R.id.navigation_profile_perusahaan
            )
            appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        } else {
            appBarConfiguration = AppBarConfiguration(setOf(R.id.fragment_login, R.id.fragment_splash))
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}