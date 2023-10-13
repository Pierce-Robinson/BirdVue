package com.varsitycollege.birdvue

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.varsitycollege.birdvue.data.HomeViewModel
import com.varsitycollege.birdvue.databinding.ActivityHomeBinding
import com.varsitycollege.birdvue.ui.CommunityFragment
import com.varsitycollege.birdvue.ui.HotspotFragment
import com.varsitycollege.birdvue.ui.ObservationsFragment
import com.varsitycollege.birdvue.ui.SettingsFragment

class HomeActivity : AppCompatActivity() {
    private var googleMap: GoogleMap? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001 // You can use any integer value here
    private lateinit var binding: ActivityHomeBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var model: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize viewmodel and bottom nav view
        model = ViewModelProvider(this)[HomeViewModel::class.java]
        bottomNavigationView = binding.bottomNavView

        //Set startup fragment, keep current fragment if dark mode changes
        if (model.getCurrentFragment() != null) {
            replaceFragment(model.getCurrentFragment()!!)
        } else {
            replaceFragment(CommunityFragment())
        }

        //Handle bottom nav view press
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.bottom_community -> {
                    replaceFragment(CommunityFragment())
                    model.setCurrentFragment(CommunityFragment())
                    true
                }
                R.id.bottom_map -> {
                    replaceFragment(HotspotFragment())
                    model.setCurrentFragment(HotspotFragment())
                    true
                }
                R.id.bottom_observations -> {
                    replaceFragment(ObservationsFragment())
                    model.setCurrentFragment(ObservationsFragment())
                    true
                }
                R.id.bottom_settings -> {
                    replaceFragment(SettingsFragment())
                    model.setCurrentFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        // Request location permissions
        //TODO: remove this check if it doesn't break stuff
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            enableMyLocation() // If permissions are already granted, enable location
//        } else {
//            // If permissions are not granted, request them
//            requestPermissions(
//                arrayOf(
//                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//                ),
//                LOCATION_PERMISSION_REQUEST_CODE
//            )
//        }

        //Floating action button
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

    }
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        } else {
            // Request location permissions
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

}