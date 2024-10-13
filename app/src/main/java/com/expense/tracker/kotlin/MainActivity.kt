package com.expense.tracker.kotlin

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.expense.tracker.kotlin.databinding.ActivityMainBinding
import com.expense.tracker.kotlin.ui.home.HomeFragment

@Suppress("UNUSED_VARIABLE")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_transaction, R.id.navigation_summary
            )
        )
        navView.setupWithNavController(navController)
    }

    fun updateHomeFragment() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
        if (currentFragment?.childFragmentManager?.fragments?.firstOrNull() is HomeFragment) {
            val homeFragment = currentFragment.childFragmentManager.fragments.first() as HomeFragment
            homeFragment.refreshData()
        }
    }
}
