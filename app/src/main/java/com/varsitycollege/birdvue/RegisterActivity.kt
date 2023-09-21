package com.varsitycollege.birdvue

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.varsitycollege.birdvue.data.User
import com.varsitycollege.birdvue.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var  auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize shared firebase auth instance
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")

        //Call register method when register is pressed
        binding.signupButton.setOnClickListener{
            register()
        }

        //Go back to login page
        binding.goBackTextView.setOnClickListener{
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }
    }

    private fun register() {
        //TODO: add validation and visualize errors once ui fixed
        try {
            val email = "" + binding.emailEditText.text
            val password = "" + binding.passwordEditText.text
            //TODO: get name from name field
            val name = "TEMP NAME"

            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    //Set display name
                    val user = auth.currentUser
                    val builder = UserProfileChangeRequest.Builder()
                    builder.displayName = name
                    val changeRequest: UserProfileChangeRequest = builder.build()
                    user!!.updateProfile(changeRequest)

                    //Create user object for user
                    writeNewUser(user.uid, name, email, 100)

                    //Sign in immediately
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if(task.isSuccessful){
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext,e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun writeNewUser(userId: String, name: String, email: String, maxDistance: Number) {
        val user = User(userId, name, email, maxDistance)
        val ref = database.getReference("users")
        ref.child(userId).setValue(user).addOnSuccessListener{
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

}