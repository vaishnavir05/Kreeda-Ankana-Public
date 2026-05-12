package com.example.kreedaankana

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.kreedaankana.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        // Wire bottom nav with navigation controller
        binding.bottomNav.setupWithNavController(navController)

        // Show/hide bottom nav based on destination
        val topLevelDestinations = setOf(
            R.id.homeFragment,
            R.id.groundListFragment,
            R.id.teamFragment,
            R.id.challengeBoardFragment,
            R.id.profileFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in topLevelDestinations) {
                binding.bottomNav.visibility = View.VISIBLE
            } else {
                binding.bottomNav.visibility = View.GONE
            }
        }

        // Check session — if already logged in, go straight to home
        val app = application as KreedaApplication
        if (app.sessionManager.isLoggedIn()) {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.homeFragment)
            navController.graph = navGraph
        }
    }
}
