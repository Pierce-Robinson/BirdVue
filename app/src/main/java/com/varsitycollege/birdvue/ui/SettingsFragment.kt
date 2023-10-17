package com.varsitycollege.birdvue.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.varsitycollege.birdvue.LoginActivity
import com.varsitycollege.birdvue.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var auth: FirebaseAuth

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        //Handle sign out
        binding.logoutButton.setOnClickListener {
            auth = FirebaseAuth.getInstance()
            val currentUser: FirebaseUser? = auth.currentUser
            if (currentUser != null) {
                logOut()
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


    private fun onDeleteAccountClick(view : View) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // this will show a confirmation popup for deletion
            AlertDialog.Builder(requireContext())
                .setTitle("delete account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes") { _, _ ->
                    // deelete the user account

                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                // account deleted successfully
                                FirebaseAuth.getInstance().signOut()
                                Toast.makeText(requireContext(), "Your account has been deleted successfully", Toast.LENGTH_SHORT).show()

                                // go to the login screen
                                val intent = Intent(requireContext(), LoginActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish() // Finish the current activity after logout
                            } else {
                                // if te account deletion failed
                                Toast.makeText(requireContext(), "Sorry we failed to delete your account. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            // if the iser isnt sigened in
            Toast.makeText(requireContext(), "It seems that you are not currently logged in.", Toast.LENGTH_SHORT).show()
        }
    }
}





