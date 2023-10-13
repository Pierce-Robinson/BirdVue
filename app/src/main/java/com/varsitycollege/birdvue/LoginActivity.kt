package com.varsitycollege.birdvue

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.varsitycollege.birdvue.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val usernameEditTextField = findViewById<TextInputLayout>(R.id.usernameEditTextField)

        usernameEditText.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // When EditText gains focus
                usernameEditTextField.setBackgroundResource(R.drawable.border_background_focused)
            } else {
                // When EditText loses focus
                usernameEditTextField.setBackgroundResource(R.drawable.border_background)
            }
        }
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val login_passwordTextField = findViewById<TextInputLayout>(R.id.login_passwordTextField)

        passwordEditText.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // When EditText gains focus
                login_passwordTextField.setBackgroundResource(R.drawable.border_background_focused)
            } else {
                // When EditText loses focus
                login_passwordTextField.setBackgroundResource(R.drawable.border_background)
            }
        }
        auth = Firebase.auth
        //If user is currently logged in, go to home page immediately
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Login to home
        binding.loginButton.setOnClickListener{
            login()
        }

        //redirect to sign up
        binding.signUpTextTextView.setOnClickListener{
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
        }
    }

    private fun login() {
        //TODO: add validation and visualize errors once ui fixed
        val email = "" + binding.usernameEditText.text
        val password = "" + binding.passwordEditText.text
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
}