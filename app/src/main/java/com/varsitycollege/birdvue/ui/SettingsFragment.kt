package com.varsitycollege.birdvue.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.varsitycollege.birdvue.LoginActivity
import com.varsitycollege.birdvue.databinding.FragmentSettingsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Reference the database
        database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        getUserData()

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
            try {
                if (maxDistanceText.isNotBlank() && maxDistanceText.toInt() > 0 && maxDistanceText.toInt() <= 50) {
                    val maxDistanceValue = maxDistanceText.toInt()
                    updateMaxDistance(maxDistanceValue)
                } else {
                    Toast.makeText(this@SettingsFragment.requireActivity().applicationContext, "Please enter a valid value (1 - 50)", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsFragment.requireActivity().applicationContext, "Please enter a valid value (1 - 50)", Toast.LENGTH_LONG).show()
            }
        }

        //Handle delete account
        binding.deleteAccountButton.setOnClickListener {
            deleteAccount()
        }

        // Add an OnCheckedChangeListener to the checkbox
        binding.metricUnitsCheckbox.setOnCheckedChangeListener { _, isChecked ->
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

    private fun deleteObservations(id: String?) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Observations")
            .setMessage("Do you also want to delete all your observations?")
            .setPositiveButton("Yes") { _, _ ->
                auth = FirebaseAuth.getInstance()
                try {
                    // Delete user's observations
                    val query = database.getReference("observations").orderByChild("userId").equalTo(id)
                    if (id != null) {
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (observationSnapshot in dataSnapshot.children) {
                                    // Delete the observation with the matching user ID
                                    observationSnapshot.ref.removeValue()
                                    Log.i("Observation deleted", "${observationSnapshot.key}")

                                }

                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e("Error while deleting observations", "${databaseError.toException()}")
                            }
                        })
                        try {
                            // Delete user's object (Before account deletion to retain permissions
                            ref = database.getReference("users")
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
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@SettingsFragment.requireActivity().applicationContext,
                                "Please login, then try again.",
                                Toast.LENGTH_LONG
                            ).show()
                            logOut()
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
            .setNegativeButton("No") {_, _ ->
                try {
                    // Delete user's object (Before account deletion to retain permissions
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
            .show()
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
                    //Ask user if they also want to delete observations
                    deleteObservations(id)
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

    //Fetch and return the max distance from the user's object and update the value in the maxDistanceEditText when the fragment is displayed
    private fun getUserData() {
        auth = FirebaseAuth.getInstance()
        val id = auth.currentUser?.uid

        if (id != null) {
            ref = database.getReference("users").child(id)

            // Add a ValueEventListener to get the maxDistance value
            // Link: https://stackoverflow.com/questions/42986449/firebase-value-event-listener
            // date: 17 October 2023
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val displayName = dataSnapshot.child("username").getValue(String::class.java)

                        // Set the display name
                        if (displayName != null && _binding != null) {
                            binding.displayNameTextView.text = displayName
                        }


                        val maxDistance = dataSnapshot.child("maxDistance").getValue(Int::class.java)
                        val metricUnits = dataSnapshot.child("metricUnits").getValue(Boolean::class.java)
                        if (maxDistance != null) {
                            val maxDistanceText = maxDistance.toString()
                            val editableMaxDistance = Editable.Factory.getInstance().newEditable(maxDistanceText)
                            if (_binding != null) {
                                binding.maxDistanceEditText.text = editableMaxDistance
                            }
                        }
                        if (metricUnits != null) {
                            if (_binding != null) {
                                binding.metricUnitsCheckbox.isChecked = metricUnits
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Database error", "databaseError.message")
                }
            })
        }
    }

}
