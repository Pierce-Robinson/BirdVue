package com.varsitycollege.birdvue

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.color.MaterialColors
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

    override fun onResume() {
        super.onResume()
        //Reset errors when view configuration changes
        binding.registerEmailTextField.error = null
        binding.registerNameTextField.error = null
        binding.registerPasswordTextField.error = null
        binding.registerConfirmTextField.error = null
    }

    private fun register() {
        try {
            //Reset errors

            //Get attribute colors
            //https://stackoverflow.com/questions/73722004/how-to-get-color-which-is-defined-as-attribute-for-different-themes-in-android
            //user answered
            //https://stackoverflow.com/users/534471/emanuel-moecklin
            //accessed 17 October 2023
            binding.registerEmailTextField.error = null
            var color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondaryContainer, Color.GRAY)
            binding.registerEmailTextField.boxBackgroundColor = color
            binding.registerEmailTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, Color.GRAY)))

            binding.registerNameTextField.error = null
            binding.registerNameTextField.boxBackgroundColor = color
            binding.registerNameTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, Color.GRAY)))

            binding.registerPasswordTextField.error = null
            binding.registerPasswordTextField.boxBackgroundColor = color
            binding.registerPasswordTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, Color.GRAY)))
            binding.registerPasswordTextField.setEndIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, Color.GRAY)))

            binding.registerConfirmTextField.error = null
            binding.registerConfirmTextField.boxBackgroundColor = color
            binding.registerConfirmTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, Color.GRAY)))
            binding.registerConfirmTextField.setEndIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, Color.GRAY)))

            val email = binding.registerEmailEditText.text.toString()
            val name = binding.nameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirm = binding.confirmPasswordEditText.text.toString()

            //Validate
            color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorErrorContainer, Color.GRAY)
            if (email.isBlank()) {
                binding.registerEmailTextField.error = "Required."
                binding.registerEmailTextField.boxBackgroundColor = color
                binding.registerEmailTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                return
            }
            if (name.isBlank()) {
                binding.registerNameTextField.error = "Required."
                binding.registerNameTextField.boxBackgroundColor = color
                binding.registerNameTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                return
            }
            if (password.isBlank()) {
                binding.registerPasswordTextField.error = "Required."
                binding.registerPasswordTextField.boxBackgroundColor = color
                binding.registerPasswordTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                binding.registerPasswordTextField.setEndIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                return
            }
            if (confirm.isBlank()) {
                binding.registerConfirmTextField.error = "Required."
                binding.registerConfirmTextField.boxBackgroundColor = color
                binding.registerConfirmTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                binding.registerConfirmTextField.setEndIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                return
            }
            if (confirm != password) {
                binding.registerConfirmTextField.error = "Passwords do not match."
                binding.registerConfirmTextField.boxBackgroundColor = color
                binding.registerConfirmTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                binding.registerConfirmTextField.setEndIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                return
            }

            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    //Set display name
                    val user = auth.currentUser
                    val builder = UserProfileChangeRequest.Builder()
                    builder.displayName = name
                    val changeRequest: UserProfileChangeRequest = builder.build()
                    user!!.updateProfile(changeRequest)

                    //Create user object for user - default distance is 5
                    writeNewUser(user.uid, name, email, 5.0, true)

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
                //Handle exception and provide feedback to user
                if(exception.localizedMessage?.contains("badly formatted") == true) {
                    binding.registerEmailTextField.error = "Email is incorrectly formatted."
                    binding.registerEmailTextField.boxBackgroundColor = color
                    binding.registerEmailTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                } else if (exception.localizedMessage?.contains("at least 6") == true) {
                    binding.registerPasswordTextField.error = "Password must be at least 6 characters long."
                    binding.registerPasswordTextField.boxBackgroundColor = color
                    binding.registerPasswordTextField.setStartIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                    binding.registerPasswordTextField.setEndIconTintList(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.GRAY)))
                }
                else {
                    Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext,e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun writeNewUser(userId: String, name: String, email: String, maxDistance: Double, metric: Boolean) {
        val user = User(userId, name, email, maxDistance, metric)
        val ref = database.getReference("users")
        ref.child(userId).setValue(user).addOnSuccessListener{
            Toast.makeText(this, "Welcome ${name}!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to register.", Toast.LENGTH_SHORT).show()
        }
    }

}