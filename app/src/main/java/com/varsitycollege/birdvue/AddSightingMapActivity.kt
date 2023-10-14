package com.varsitycollege.birdvue

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.varsitycollege.birdvue.data.Observation
import com.varsitycollege.birdvue.databinding.ActivityAddSightingMapBinding
import java.io.ByteArrayOutputStream
import java.io.File

class AddSightingMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSightingMapBinding
    private var downloadUrl: String? = null
    private lateinit var photoFile: File
    private lateinit var photoPreview: ImageView
    private val REQUEST_IMAGE_CAPTURE = 1
    private var selectedImageBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSightingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // registers a photo picker activity launcher in single select mode.
        // Link: https://developer.android.com/training/data-storage/shared/photopicker
        // accessed: 13 October 2023
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // this callback is invoked after they choose an image or close the photo picker

            binding.overviewSubmitButton.setOnClickListener {
                try {
                    if (uri != null){
                        val database =FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
                        val ref = database.getReference("observations")
                        Log.d("PhotoPicker", "Selected URI: $uri")
                        val key = ref.push().key
                        val observation = Observation(
                            id = key!!, // use a unique ID or use push() in Firebase
                            birdName = binding.birdNameFieldEditText.text.toString(),
                            date = binding.datePicker1.toString(),
                            photo = downloadUrl,
                            details = binding.detailsFieldEditText.text.toString(),
                            lat = 0.0, // daniel please provide actual latitude
                            lng = 0.0, // daniel please provide actual longitude
                            location = "Your Location",
                            likes = 0, // ahd to put something here because of the data class
                            comments = emptyList(), // theres no comments for now i know
                            userId = FirebaseAuth.getInstance().currentUser!!.uid
                        )


                        // pushing the data fpr observation to firebase
                        // link: https://www.geeksforgeeks.org/how-to-save-data-to-the-firebase-realtime-database-in-android/
                        // accessed: 13 October 2023
                        if (key != null) {
                            ref.child(key).setValue(observation).addOnSuccessListener {
                                Toast.makeText(
                                    applicationContext,
                                    "Observation was added successfully.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.addOnFailureListener { e ->
                                Toast.makeText(
                                    applicationContext,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }


                    } else {
                        Log.d("PhotoPicker", "No media selected")
                    }
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.openGalleryButton.setOnClickListener {
            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // val imageBitmap = data?.extras?.get("data") as Bitmap
            val imageBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            photoPreview.setImageBitmap(imageBitmap)

            uploadImageToFirebaseStorage(imageBitmap)
        }
    }
    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap) {
        // Create a unique file name for the image
        val fileName = "photo_${System.currentTimeMillis()}.jpg"

        // Get a reference to the Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("images/")

        // Create a reference to the file location in Firebase Storage
        val imageRef = storageRef.child(fileName)

        // Convert the Bitmap to bytes
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        // Upload the image to Firebase Storage
        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Image upload successful
                Toast.makeText(
                    this,
                    "Image uploaded to Firebase Storage",
                    Toast.LENGTH_SHORT
                ).show()

                imageRef.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uri = task.result
                        downloadUrl = uri.toString()

                    } else {
                        // Image upload failed
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
