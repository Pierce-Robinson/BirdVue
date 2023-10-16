package com.varsitycollege.birdvue.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.varsitycollege.birdvue.data.HomeViewModel
import com.varsitycollege.birdvue.data.HotspotAdapter

class HotspotFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener  {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001 // You can use any integer value here
    private var googleMap: GoogleMap? = null
    private var _binding: FragmentHotspotBinding? = null
    private lateinit var userLocation: LatLng
    private lateinit var model: HomeViewModel

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentHotspotBinding.inflate(inflater, container, false)

        //Initialize viewModel
        model = ViewModelProvider(this)[HomeViewModel::class.java]

        //Bottom drawer
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomNavigationContainer)
        bottomSheetBehavior.peekHeight = 350

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
        // Async map
        supportMapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            // When map is loaded
            googleMap.setOnMapClickListener { latLng ->
                // When clicked on map
                // Initialize marker options
                //val markerOptions = MarkerOptions()
                // Set position of marker
                //markerOptions.position(latLng)
                // Set title of marker
                //markerOptions.title("${latLng.latitude} : ${latLng.longitude}")
                // Remove all markers
                //googleMap.clear()
                // Animating to zoom the marker
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                // Add marker on map
                //googleMap.addMarker(markerOptions)
            }
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled =true

            googleMap.setOnMarkerClickListener {
                //Maybe initialize navigation for the hotspot markers here
                false
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
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        userLocation = LatLng(it.latitude, it.longitude)
                        //Get hotspot data after user's location is set
                        getHotspotData()
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    }
                }
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

        enableMyLocation()
    }

    private fun getHotspotData() {
        //TODO: Change distance to user's selected setting
        //TODO: BUG This is always empty at the moment
        if (model.getHotspotList().isNotEmpty()) {
            //Get hotspots from viewmodel
            Toast.makeText(this@HotspotFragment.requireActivity().applicationContext, "Getting data from viewmodel", Toast.LENGTH_LONG).show()
            Log.i("Get Hotspot", "Get Hotspot from viewmodel")
            //TODO: Remember to set saved hotspot list to null again if user changes distance
            val hotspotData = model.getHotspotList()
            //Add hotspot to recycler view
            val hotspotAdapter = HotspotAdapter(hotspotData)
            binding.hotspotRecycler.adapter = hotspotAdapter
            binding.hotspotRecycler.layoutManager = LinearLayoutManager(requireContext())
            for (h in hotspotData) {
                //Add hotspot to map
                if (h.lat != null && h.lng != null) {
                    val pos = LatLng(h.lat, h.lng)
                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title(h.locName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                }

            }
        } else {
            Toast.makeText(this@HotspotFragment.requireActivity().applicationContext, "Getting data from API", Toast.LENGTH_LONG).show()
            //Call eBird api to fetch hotspot data
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.ebird.org/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(EBirdAPI::class.java)

            api.getHotspots(userLocation.latitude, userLocation.longitude, "json", 5, BuildConfig.EBIRD_API_KEY).enqueue(object : Callback<List<Hotspot>> {
                override fun onResponse(call: Call<List<Hotspot>>, response: Response<List<Hotspot>>) {
                    val hotspotData = response.body()
                    if (hotspotData != null) {
                        //Update viewmodel list
                        model.updateHotspotList(hotspotData)
                        //Add hotspot to recycler view
                        val hotspotAdapter = HotspotAdapter(hotspotData)
                        binding.hotspotRecycler.adapter = hotspotAdapter
                        binding.hotspotRecycler.layoutManager = LinearLayoutManager(requireContext())
                        for (h in hotspotData) {
                            //Add hotspot to map
                            if (h.lat != null && h.lng != null) {
                                val pos = LatLng(h.lat, h.lng)
                                googleMap?.addMarker(
                                    MarkerOptions()
                                        .position(pos)
                                        .title(h.locName)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                )
                            }
                        }
                        var text = ""
                        for (m in model.getHotspotList()!!) {
                            text += m.locName + "_"
                        }
                        Toast.makeText(this@HotspotFragment.requireActivity().applicationContext, text, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<List<Hotspot>>, t: Throwable) {
                    Log.e("API Error", t.toString())
                }

            })
        }
    }

    //TODO: fix dark mode switch and map
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        if (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
//            // Dark mode is active
//            googleMap?.let { enableDarkMode(it) }
//        } else {
//            // Light mode is active
//            googleMap?.let { disableDarkMode(it) }
//        }
//    }

    private fun enableDarkMode(googleMap: GoogleMap) {
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(), R.raw.map_style_dark
            )
        )
    }

    private fun disableDarkMode(googleMap: GoogleMap) {
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(), R.raw.map_style_default
            )
        )
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