package com.varsitycollege.birdvue

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.view.View
import com.google.firebase.storage.FirebaseStorage
import com.varsitycollege.birdvue.BuildConfig.GOOGLE_MAPS_API_KEY
import com.varsitycollege.birdvue.data.Observation
import com.varsitycollege.birdvue.databinding.ActivityAddSightingMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class AddSightingMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {

    private lateinit var binding: ActivityAddSightingMapBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001 // You can use any integer value here
    private var googleMap: GoogleMap? = null
    private lateinit var userLocation: LatLng
    private lateinit var selectedLocation: LatLng

    private var uriMap: Uri? = null

    private var downloadUrl: String? = null
    private var downloadUrlMap: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSightingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureMap()

        // registers a photo picker activity launcher in single select mode.
        // Link: https://developer.android.com/training/data-storage/shared/photopicker
        // accessed: 13 October 2023
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // this callback is invoked after they choose an image or close the photo picker
                //Set the image of the UI imageview
                binding.openGalleryButton.setImageURI(uri)
                //On submit, upload image then observation
                binding.overviewSubmitButton.setOnClickListener {
                    if (uri != null) {
                        binding.overviewSubmitButton.isEnabled = false




                        uploadImage(uri)
                        uploadImageMap(uriMap)
                    } else {
                        Log.d("PhotoPicker", "No media selected")
                    }
                }
            }

        binding.openGalleryButton.setOnClickListener {
            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

    }

    private fun configureMap() {
        val supportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
        // Async map
        supportMapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            // Allows user to select a custom location instead of their current location
            googleMap.setOnMapClickListener { latLng ->
                // When clicked on map
                // Update selected location
                selectedLocation = latLng
                // Initialize marker options
                val markerOptions = MarkerOptions()
                // Set position of marker
                markerOptions.position(latLng)
                // Set title of marker
                markerOptions.title("${latLng.latitude} : ${latLng.longitude}")
                // Remove all markers
                googleMap.clear()
                // Animating to zoom the marker
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                // Add marker on map
                googleMap.addMarker(markerOptions)
            }
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
        })
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
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        userLocation = LatLng(it.latitude, it.longitude)
                        selectedLocation = userLocation
                        googleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                userLocation,
                                15f
                            )
                        )
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

    override fun onMyLocationButtonClick(): Boolean {
        // Handle the button click here
        return false // Return false to let the default behavior occur
    }

    private fun uploadImage(imageUri: Uri?) {
        // Generate a file name based on current time in milliseconds
        val fileName = "photo_${System.currentTimeMillis()}"
        // Get a reference to the Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("images/")
        // Create a reference to the file location in Firebase Storage
        val imageRef = storageRef.child(fileName)

        val uploadTask = imageRef.putFile(imageUri!!)
        uploadTask.addOnCompleteListener {
            if (it.isSuccessful) {
                // Image upload successful
                imageRef.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uri = task.result
                        downloadUrl = uri.toString()
                        //Upload observation after getting the download URL
                        submitObservation()
                    } else {
                        // Image upload failed
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        uploadTask.addOnFailureListener {
            Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun submitObservation() {
        try {
            val dp = binding.datePicker1
            val database =
                FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
            val ref = database.getReference("observations")
            val key = ref.push().key
            val observation = Observation(
                id = key!!, // use a unique ID or use push() in Firebase
                birdName = binding.birdNameFieldEditText.text.toString(),
                date = "${dp.year}/${dp.month + 1}/${dp.dayOfMonth}",
                photo = downloadUrl,
                details = binding.detailsFieldEditText.text.toString(),
                lat = selectedLocation.latitude,
                lng = selectedLocation.longitude,
                location = downloadUrlMap,
                likes = 0, // ahd to put something here because of the data class
                comments = emptyList(), // theres no comments for now i know
                userId = FirebaseAuth.getInstance().currentUser!!.uid
            )

            // pushing the data for observation to firebase
            // link: https://www.geeksforgeeks.org/how-to-save-data-to-the-firebase-realtime-database-in-android/
            // accessed: 13 October 2023
            ref.child(key).setValue(observation).addOnSuccessListener {
                binding.overviewSubmitButton.isEnabled = true
                Toast.makeText(
                    applicationContext,
                    "Observation was added successfully.",
                    Toast.LENGTH_LONG
                ).show()
                //Go back to the home page
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }.addOnFailureListener { e ->
                Toast.makeText(
                    applicationContext,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    fun saveStaticMapImage(
        apiKey: String,
        center: String,
        zoom: Int,
        size: String,
        scale: Int,
        format: String,
        filePath: String
    ): Uri? {
        val url = "https://maps.googleapis.com/maps/api/staticmap?" +
                "center=$center&" +
                "zoom=$zoom&" +
                "size=$size&" +
                "scale=$scale&" +
                "format=$format&" +
                "key=$apiKey"

        val imageUrl = URL(url)
        val imageStream = imageUrl.openStream()
        val output = FileOutputStream(filePath)

        imageStream.use { input ->
            output.use { fileOut ->
                input.copyTo(fileOut)
            }
        }
        return Uri.fromFile(File(filePath))
    }

    private fun uploadImageMap(imageUri: Uri?) {
        val apiKey = "$GOOGLE_MAPS_API_KEY"
        val center =
            "${selectedLocation.latitude},${selectedLocation.longitude}" // Latitude,Longitude
        val zoom = 13
        val size = "640x480"
        val scale = 2
        val format = "png"
        val filePath = "photoMap_${System.currentTimeMillis()}"
        uriMap = saveStaticMapImage(apiKey, center, zoom, size, scale, format, filePath)

        // Generate a file name based on current time in milliseconds
        val fileName = "photo_${System.currentTimeMillis()}"
        // Get a reference to the Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("images/")
        // Create a reference to the file location in Firebase Storage
        val imageRef = storageRef.child(fileName)

        val uploadTask = imageRef.putFile(imageUri!!)
        uploadTask.addOnCompleteListener {
            if (it.isSuccessful) {
                // Image upload successful
                imageRef.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uri = task.result
                        downloadUrl = uri.toString()
                        //Upload observation after getting the download URL
                        submitObservation()
                    } else {
                        // Image upload failed
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        uploadTask.addOnFailureListener {
            Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
}
