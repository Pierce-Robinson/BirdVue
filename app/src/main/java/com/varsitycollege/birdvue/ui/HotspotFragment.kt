package com.varsitycollege.birdvue.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.varsitycollege.birdvue.BuildConfig
import com.varsitycollege.birdvue.R
import com.varsitycollege.birdvue.api.EBirdAPI
import com.varsitycollege.birdvue.data.Hotspot
import com.varsitycollege.birdvue.databinding.FragmentHotspotBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.google.android.gms.maps.UiSettings;
class HotspotFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener  {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001 // You can use any integer value here
    private var googleMap: GoogleMap? = null
    private var _binding: FragmentHotspotBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentHotspotBinding.inflate(inflater, container, false)

        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        supportMapFragment?.getMapAsync(this)
        // Async map
        supportMapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            // When map is loaded
            googleMap.setOnMapClickListener { latLng ->
                // When clicked on map
                // Initialize marker options
                val markerOptions = MarkerOptions()
                // Set position of marker
                markerOptions.position(latLng)
                // Set title of marker
                markerOptions.title("${latLng.latitude} : ${latLng.longitude}")
                // Remove all markers
                googleMap.clear()
                // Animating to zoom the marker
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                // Add marker on map
                googleMap.addMarker(markerOptions)
            }
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled =true
        })



        //Call api to fetch session token for unique questions
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.ebird.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(EBirdAPI::class.java)
        api.getHotspots(-25.74, 28.22, "json", 10, BuildConfig.EBIRD_API_KEY).enqueue(object : Callback<List<Hotspot>> {
            override fun onResponse(call: Call<List<Hotspot>>, response: Response<List<Hotspot>>) {
                val hotspotData = response.body()
                var text = ""
                if (hotspotData != null) {
                    for (h in hotspotData) {
                        text += h.locName + "\n"
                    }
                }
                binding.apiTest.text = text
            }

            override fun onFailure(call: Call<List<Hotspot>>, t: Throwable) {
                Log.e("Token error", t.toString())
            }

        })

        return binding.root
    }
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable my location
                enableMyLocation()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setOnMapClickListener { latLng ->
            // Your existing code for handling map clicks
        }
        enableMyLocation()
    }

    override fun onMyLocationButtonClick(): Boolean {
        // Handle the button click here
        return false // Return false to let the default behavior occur
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}