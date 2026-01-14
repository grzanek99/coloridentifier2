package com.example.coloridentifier

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.coloridentifier.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * glowna aktywnosc aplikacji z nawigacja
 */
class MainActivity : AppCompatActivity() {

    // binding dla layoutu aktywnosci
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // inflatuje layout za pomoca view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // ustawia widok jako content aktywnosci
        setContentView(binding.root)

        // pobiera fragment hosta nawigacji z fragmentmanagera
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // pobiera kontroller nawigacji z fragmentu
        val navController = navHostFragment.navController

        // pobiera dolna nawigacje z bindingu
        val bottomNav: BottomNavigationView = binding.bottomNavigation
        // laczy dolna nawigacje z kontrollerem nawigacji
        bottomNav.setupWithNavController(navController)

        // dodaje listener zmian destynacji nawigacji
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // aktualizuje tytul toolbara na etykiete destynacji
            binding.toolbar.title = destination.label
        }
    }
}
