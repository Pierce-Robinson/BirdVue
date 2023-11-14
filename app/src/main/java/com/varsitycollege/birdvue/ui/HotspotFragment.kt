package com.varsitycollege.birdvue.ui

import android.content.pm.PackageManager
import kotlin.math.*
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varsitycollege.birdvue.data.HomeViewModel
import com.varsitycollege.birdvue.data.HotspotAdapter
import com.varsitycollege.birdvue.data.Observation
import java.text.DecimalFormat

class HotspotFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener  {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001 // You can use any integer value here
    private var googleMap: GoogleMap? = null
    private var _binding: FragmentHotspotBinding? = null
    private lateinit var userLocation: LatLng
    private val model: HomeViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHotspotBinding.inflate(inflater, container, false)

        //Observe viewModel
        //https://developer.android.com/topic/libraries/architecture/livedata
        //Accessed 18 October 2023

        val hotspotObserver = Observer<List<Hotspot>> {
            //Toast.makeText(this@HotspotFragment.requireActivity().applicationContext, "viewmodel updated", Toast.LENGTH_LONG).show()
        }
        //model.hotspotList.observe(viewLifecycleOwner, hotspotObserver)

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
                // Animating to zoom the marker
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
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
                        //https://stackoverflow.com/questions/28672883/java-lang-illegalstateexception-fragment-not-attached-to-activity
                        //stops the app crashing before activity is ready
                        val activity = activity
                        if (isAdded && activity != null) {
                            getHotspotData()
                        }
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
                //https://stackoverflow.com/questions/28672883/java-lang-illegalstateexception-fragment-not-attached-to-activity
                //stops the app crashing before activity is ready
                val activity = activity
                if (isAdded && activity != null) {
                    enableMyLocation()
                }

            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        //https://stackoverflow.com/questions/28672883/java-lang-illegalstateexception-fragment-not-attached-to-activity
        //stops the app crashing before activity is ready
        val activity = activity
        if (isAdded && activity != null) {
            enableMyLocation()
        }
    }

    private fun getHotspotData() {
        if (model.hotspotList.value != null) {
            //Get hotspots from viewmodel
            val hotspotData = model.hotspotList.value
            Log.i("Get Hotspot", "Get Hotspot from viewmodel")
            //Add hotspot to recycler view
            val hotspotAdapter = HotspotAdapter(hotspotData!!)
            binding.hotspotRecycler.adapter = hotspotAdapter
            binding.hotspotRecycler.layoutManager = LinearLayoutManager(requireContext())
            //Update distance text view
            if (_binding != null) {
                if (model.metric.value == true) {
                    binding.distanceTextView.text = "Showing hotspots within ${model.currentDistance.value} km"
                } else {
                    binding.distanceTextView.text = "Showing hotspots within ${model.currentDistance.value} miles"
                }
            }
            for (h in hotspotData) {
                //Add hotspot to map
                if (h.lat != null && h.lng != null) {
                    val pos = LatLng(h.lat, h.lng)
                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title(h.locName)
                            .snippet("Observed species: " + h.numSpeciesAllTime)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                }
            }
            //Populate map with user observations
            if (model.currentDistance.value != null) {
                fetchObservations(model.currentDistance.value!!)
            }
        } else {
            getUserDistance()
        }
    }

    private fun getUserDistance() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
        ref = database.getReference("users")
        val id = auth.currentUser?.uid
        try {
            if (id != null) {
                ref.child(id).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var distance = snapshot.child("maxDistance").getValue(Double::class.java)
                            val metric = snapshot.child("metricUnits").getValue(Boolean::class.java)

                            if (distance != null) {
                                //Clear hotspot cache
                                model.hotspotList.value = null
                                //Convert to imperial if needed, then fetch hotspots from API
                                model.currentDistance.value = distance
                                if (metric != null && !metric) {
                                    if (_binding != null) {
                                        binding.distanceTextView.text = "Showing hotspots within $distance miles"
                                    }
                                    distance = convertToImperial(distance)
                                    model.metric.value = false
                                    Log.i("Distance conversion", "Converted to imperial")
                                } else {
                                    if (_binding != null) {
                                        binding.distanceTextView.text = "Showing hotspots within $distance km"
                                    }
                                    model.metric.value = true
                                }
                                fetchHotspots(distance)
                                fetchObservations(distance)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Database error", error.details)
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("User distance error", "" + e.localizedMessage)
        }
    }

    private fun convertToImperial(distance: Double): Double {
        val result = distance * 1.60934
        val df = DecimalFormat("#.##")
        return df.format(result).toDouble()
    }

    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // Earth radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c // Distance in kilometers
    }

    private fun fetchObservations(distance: Double) {
        database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
        ref = database.getReference("observations")
        try {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (observationSnapshot in snapshot.children) {
                            val observation = observationSnapshot.getValue(Observation::class.java)
                            if (observation?.lat != null && observation.lng != null) {
                                if (calculateDistance(userLocation.latitude, userLocation.longitude, observation.lat, observation.lng) <= distance) {
                                    //Log.i("Found Observation", observation.birdName + ": " + observation.details)
                                    val pos = LatLng(observation.lat, observation.lng)
                                    googleMap?.addMarker(
                                        MarkerOptions()
                                            .position(pos)
                                            .title(observation.birdName)
                                            .snippet("Spotted: " + observation.date)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                    )
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Database error", error.details)
                }
            })
        } catch (e: Exception) {
            Log.e("Error fetching observations", "" + e.localizedMessage)
        }
    }

    private fun fetchHotspots(distance: Double) {
        //Toast.makeText(this@HotspotFragment.requireActivity().applicationContext, "Getting data from API", Toast.LENGTH_LONG).show()
        //Call eBird api to fetch hotspot data
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.ebird.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(EBirdAPI::class.java)
        api.getHotspots(userLocation.latitude, userLocation.longitude, "json", distance, BuildConfig.EBIRD_API_KEY).enqueue(object : Callback<List<Hotspot>> {
            override fun onResponse(call: Call<List<Hotspot>>, response: Response<List<Hotspot>>) {
                try {
                    val hotspots = response.body()
                    if (hotspots != null) {
                        //Update viewmodel list
                        model.hotspotList.value = hotspots
                        //Add hotspot to recycler view
                        val hotspotAdapter = HotspotAdapter(hotspots)
                        binding.hotspotRecycler.adapter = hotspotAdapter
                        binding.hotspotRecycler.layoutManager = LinearLayoutManager(requireContext())
                        for (h in hotspots) {
                            //Add hotspot to map
                            if (h.lat != null && h.lng != null) {
                                val pos = LatLng(h.lat, h.lng)
                                googleMap?.addMarker(
                                    MarkerOptions()
                                        .position(pos)
                                        .title(h.locName)
                                        .snippet("Observed species: " + h.numSpeciesAllTime)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.message?.let { Log.e("Hotspot Error", it) }
                }
            }

            override fun onFailure(call: Call<List<Hotspot>>, t: Throwable) {
                Log.e("API Error", t.toString())
            }

        })
    }

    //TODO: adding uiMode in manifest breaks dark mode switching
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