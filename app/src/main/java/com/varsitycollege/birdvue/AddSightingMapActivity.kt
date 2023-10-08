package com.varsitycollege.birdvue

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.varsitycollege.birdvue.databinding.ActivityAddSightingMapBinding

class AddSightingMapActivity : AppCompatActivity() {
    private val galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private lateinit var binding: ActivityAddSightingMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSightingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openGalleryButton.setOnClickListener {
            if (isGalleryPermissionGranted()) {
                openGallery()
            } else {
                requestGalleryPermission()
            }
        }
    }

    private fun isGalleryPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, galleryPermission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestGalleryPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(galleryPermission), galleryPermissionRequestCode)
    }

    private val galleryPermissionRequestCode = 101

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == galleryPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Gallery permission is required to open this gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val openGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Handle the result when an image is selected from the gallery
            val data: Intent? = result.data
            // Handle the selected image here
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        openGalleryLauncher.launch(galleryIntent)
    }
}
