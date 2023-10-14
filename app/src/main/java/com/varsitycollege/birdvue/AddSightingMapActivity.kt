package com.varsitycollege.birdvue

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.varsitycollege.birdvue.data.Observation
import com.varsitycollege.birdvue.databinding.ActivityAddSightingMapBinding

class AddSightingMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSightingMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSightingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database =FirebaseDatabase.getInstance()
        val ref = database.getReference("observations")

        // registers a photo picker activity launcher in single select mode.
        // Link: https://developer.android.com/training/data-storage/shared/photopicker
        // accessed: 13 October 2023
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // this callback is invoked after they choose an image or close the photo picker
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                val birdName = binding.birdNameFieldEditText.text.toString()
                val details = binding.detailsFieldEditText.text.toString()
                val date = "" //we get this date from the date picekr
                val photoUrl = uri.toString()

                val observation = Observation(
                    id = "", // use a unique ID or use push() in Firebase
                    birdName = birdName,
                    date = date,
                    photo = photoUrl,
                    details = details,
                    lat = 0.0, // daniel please provide actual latitude
                    lng = 0.0, // daniel please provid actual longitude
                    location = "Your Location",
                    likes = 0, // ahd to put something here because of the data class
                    comments = emptyList() // theres no comments for now i know
                )

                // pushing the data fpr observation to firebase
                // link: https://www.geeksforgeeks.org/how-to-save-data-to-the-firebase-realtime-database-in-android/
                // accessed: 13 October 2023
                val key = ref.push().key
                if (key != null) {
                    ref.child(key).setValue(observation).addOnSuccessListener {
                        Toast.makeText(applicationContext, "Observation was added successfully.", Toast.LENGTH_LONG).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

        
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
