package com.varsitycollege.birdvue

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.varsitycollege.birdvue.databinding.ActivityAddSightingMapBinding
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

var mapView: MapView? = null

class AddSightingMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSightingMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSightingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Registers a photo picker activity launcher in single-select mode.
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
        
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        binding.openGalleryButton.setOnClickListener {
            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

//        binding.XXXXX.setOnClickListener {
//            // Write a task to the database
//            try {
//
//                database = FirebaseDatabase.getInstance("XXXXXXX")
//                ref = database.getReference("XXXXXX")
//                val key = ref.push().key
//                val task = XXXXXXXXX(
//                    id = key!!,
//                    XXXXXXX = binding.XXXXXXXXX.text.toString(),
//                    XXXXXX = binding.XXXXXXX.text.toString(),
//                    XXXXXXX = binding.XXXXX.text.toString(),
//                    XXXXXXX = binding.XXXX.text.toString(),
//                    imageId = downloadUrl,
//                    userId = FirebaseAuth.getInstance().currentUser!!.uid
//                )
//                XXXXXX.id?.let { it1 ->
//                    ref.child(it1).setValue(XXXX).addOnSuccessListener {
//                        Toast.makeText(applicationContext, "Success.", Toast.LENGTH_LONG).show()
//                    }
//                }
//            } catch (e: Exception) {
//                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
//            }
//        }
    }
}
