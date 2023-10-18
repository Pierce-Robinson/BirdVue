package com.varsitycollege.birdvue.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.varsitycollege.birdvue.LoginActivity
import com.varsitycollege.birdvue.databinding.FragmentSettingsBinding
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var metricUnitsCheckbox: MaterialCheckBox
    private lateinit var maxDistanceEditText: EditText

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // liam referenced the database
        database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")

        // Initialize checkbox reference
        metricUnitsCheckbox = binding.metricUnitsCheckbox

        // TODO Liam: change this to fetch the boolean value from the user's firebase object (see method below)
        metricUnitsCheckbox.isChecked = true



        //Handle sign out
        binding.logoutButton.setOnClickListener {
            auth = FirebaseAuth.getInstance()
            val currentUser: FirebaseUser? = auth.currentUser
            if (currentUser != null) {
                logOut()
            }
        }
    // Handle the update max distance
        binding.updateMaxDistanceButton.setOnClickListener {
            val maxDistanceText = binding.maxDistanceEditText.text.toString()
            if (maxDistanceText.isNotBlank()) {
                val maxDistanceValue = maxDistanceText.toInt()
                // store the input into a variable and then return the method here
                updateMaxDistance(maxDistanceValue)
            } else {
                Toast.makeText(requireContext(), "Please enter a valid value", Toast.LENGTH_LONG).show()
            }
        }


        //Handle delete account
        binding.deleteAccountButton.setOnClickListener {
            deleteAccount()
        }

        // Add an OnCheckedChangeListener to the checkbox
        metricUnitsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // User selected Metric units
                updateMetricUnits(true)
            } else {
                // User selected Imperial units
                updateMetricUnits(false)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logOut() {
        auth.signOut()
        activity?.let {
            val intent = Intent(it, LoginActivity::class.java)
            it.startActivity(intent)
            it.finish() // Finish the current activity after logout
        }
    }

    private fun deleteAccount() {
        // this will show a confirmation popup for deletion
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Yes") { _, _ ->
                // delete the user account
                auth = FirebaseAuth.getInstance()
                try {
                    val id = auth.currentUser?.uid
                    // Delete user's object (Before account deletion to retain permissions
                    database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
                    ref = database.getReference("users")
                    if (id != null) {
                        ref.child(id).removeValue().addOnSuccessListener {
                            // On object deletion success, delete user account and return to the login screen
                            auth.currentUser?.delete()?.addOnSuccessListener {
                                activity?.let {
                                    val intent = Intent(it, LoginActivity::class.java)
                                    it.startActivity(intent)
                                    it.finish() // Finish the current activity after logout
                                }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(
                                this@SettingsFragment.requireActivity().applicationContext,
                                it.localizedMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@SettingsFragment.requireActivity().applicationContext,
                        "Please login, then try again.",
                        Toast.LENGTH_LONG
                    ).show()
                    logOut()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun updateMetricUnits(metric: Boolean) {
        auth = FirebaseAuth.getInstance()
        try {
            val id = auth.currentUser?.uid
            database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
            ref = database.getReference("users")
            if (id != null) {
                ref.child(id).child("metricUnits").setValue(metric)
            }

        } catch (e: Exception) {
            Toast.makeText(
                this@SettingsFragment.requireActivity().applicationContext,
                e.localizedMessage,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateMaxDistance(dist: Int) {
        auth = FirebaseAuth.getInstance()
        try {
            val id = auth.currentUser?.uid
            database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
            ref = database.getReference("users")
            if (id != null) {

                ref.child(id).child("maxDistance").setValue(dist)
            }
        } catch (e: Exception) {
            Toast.makeText(
                this@SettingsFragment.requireActivity().applicationContext,
                e.localizedMessage,
                Toast.LENGTH_LONG
            ).show()
        }
    }


    //TODO: And these
    //This should fetch and return the max distance from the user's object and update the value in the maxDistanceEditText when the fragment is displayed
    private fun getMaxDistance(): Int {
        auth = FirebaseAuth.getInstance()
        val id = auth.currentUser?.uid

        if (id != null) {
            database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
            ref = database.getReference("users").child(id).child("maxDistance")

            // Add a ValueEventListener to get the maxDistance value
            // Link: https://stackoverflow.com/questions/42986449/firebase-value-event-listener
            // date: 17 October 2023
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val maxDistance = dataSnapshot.getValue(Int::class.java)
                        if (maxDistance != null) {
                            val maxDistanceText = maxDistance.toString()
                            val editableMaxDistance = Editable.Factory.getInstance().newEditable(maxDistanceText)
                            maxDistanceEditText.text = editableMaxDistance
                        }

                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //any error messages can go here
                    val errorMessage = "database error: " + databaseError.message
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }
        return 0
    }


    //this should fetch the boolean value from the user's firebase object and update the checkbox when the fragment is displayed
    private fun getMetricUnits(): Boolean {

        return true
    }
}
