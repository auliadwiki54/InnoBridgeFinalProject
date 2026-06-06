package com.example.finalprojectinnobridge.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
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
            
            val bottomNavDestinations = if (role == Constants.ROLE_MAHASISWA) {
                setOf(R.id.navigation_home, R.id.navigation_chat, R.id.navigation_my_proposal, R.id.navigation_profile)
            } else {
                setOf(R.id.navigation_dashboard, R.id.navigation_chat, R.id.navigation_proposal_list, R.id.navigation_profile)
            }

            if (destination.id in bottomNavDestinations) {
                binding.bottomNavigation.visibility = View.VISIBLE
            } else {
                binding.bottomNavigation.visibility = View.GONE
            }
        }
    }

    fun updateBottomNavigation() {
        val sessionManager = SessionManager(this)
        val role = sessionManager.getUserRole()

        if (role == Constants.ROLE_MAHASISWA) {
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
            val topLevelDestinations = setOf(
                R.id.navigation_home, R.id.navigation_chat, R.id.navigation_my_proposal, R.id.navigation_profile
            )
            appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        } else if (role == Constants.ROLE_PERUSAHAAN) {
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_perusahaan)
            val topLevelDestinations = setOf(
                R.id.navigation_dashboard, R.id.navigation_chat, R.id.navigation_proposal_list, R.id.navigation_profile
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