package com.example.finalprojectinnobridge.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.ActivityMainBinding
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.utils.NotificationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            NotificationHelper.createNotificationChannels(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        checkNotificationPermission()
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

            binding.bottomNavigation.visibility = if (shouldShow) View.VISIBLE else View.GONE
            binding.layoutToolbarCustom.visibility = View.VISIBLE
            supportActionBar?.setDisplayShowTitleEnabled(false)

            // Hide AppBarLayout on screens that define their own custom MaterialToolbars
            val noAppBarDestinations = setOf(
                R.id.fragment_splash,
                R.id.fragment_login,
                R.id.fragment_register,
                R.id.navigation_chat_room,
                R.id.navigation_chat_room_perusahaan
            )
            binding.appBarLayout.visibility = if (destination.id in noAppBarDestinations) View.GONE else View.VISIBLE

            if (destination.id in noAppBarDestinations) {
                NotificationHelper.stopListening()
            }

            // Restore standard status bar theme configurations matching the dark navy headers
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            window.statusBarColor = resources.getColor(R.color.primary_navy, theme)
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

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationHelper.createNotificationChannels(this)
            }
        } else {
            NotificationHelper.createNotificationChannels(this)
        }
    }

    fun updateBottomNavigation() {
        val sessionManager = SessionManager(this)
        val role = sessionManager.getUserRole()
        val userId = sessionManager.getUserId()

        binding.bottomNavigation?.menu?.clear()

        if (role == Constants.ROLE_MAHASISWA) {
            binding.bottomNavigation?.inflateMenu(R.menu.bottom_nav_menu)
            appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_home, R.id.navigation_chat, R.id.navigation_challenge, R.id.navigation_my_proposal, R.id.navigation_profile)
            )
            if (!userId.isNullOrEmpty()) {
                NotificationHelper.startListening(this, userId, role)
            }
        } else if (role == Constants.ROLE_PERUSAHAAN) {
            binding.bottomNavigation?.inflateMenu(R.menu.bottom_nav_menu_perusahaan)
            appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_dashboard, R.id.navigation_chat_perusahaan, R.id.navigation_add_challenge, R.id.navigation_proposal_list, R.id.navigation_profile_perusahaan)
            )
            if (!userId.isNullOrEmpty()) {
                NotificationHelper.startListening(this, userId, role)
            }
        } else {
            appBarConfiguration = AppBarConfiguration(setOf(R.id.fragment_login, R.id.fragment_splash))
            NotificationHelper.stopListening()
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationHelper.stopListening()
    }
}