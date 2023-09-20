package com.varsitycollege.birdvue

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.varsitycollege.birdvue.databinding.ActivityHomeBinding
import com.varsitycollege.birdvue.ui.CommunityFragment
import com.varsitycollege.birdvue.ui.HotspotFragment
import com.varsitycollege.birdvue.ui.ObservationsFragment
import com.varsitycollege.birdvue.ui.SettingsFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Default fragment
        replaceFragment(HotspotFragment())

        bottomNavigationView = binding.bottomNavView
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.bottom_community -> {
                    replaceFragment(CommunityFragment())
                    true
                }
                R.id.bottom_map -> {
                    replaceFragment(HotspotFragment())
                    true
                }
                R.id.bottom_observations -> {
                    replaceFragment(ObservationsFragment())
                    true
                }
                R.id.bottom_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

}