package com.varsitycollege.birdvue.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varsitycollege.birdvue.R
import com.varsitycollege.birdvue.data.ImagePagerAdapter
import com.varsitycollege.birdvue.data.Observation
import com.varsitycollege.birdvue.data.ObservationAdapter
import com.varsitycollege.birdvue.databinding.FragmentCommunityBinding
import com.varsitycollege.birdvue.databinding.FragmentObservationsBinding

class ObservationsFragment : Fragment() {

    private var _binding: FragmentObservationsBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var observationArrayList: ArrayList<Observation>
    private lateinit var observationRecyclerView: RecyclerView


    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentObservationsBinding.inflate(inflater, container, false)
        observationRecyclerView = binding.recyclerView
        observationArrayList = arrayListOf()


        observationRecyclerView.layoutManager = LinearLayoutManager(context)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            getData(currentUser)
        }
        return binding.root
    }


    private fun getData(user: FirebaseUser) {
        database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
        ref = database.getReference("observations")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (observationSnapshot in snapshot.children) {
                        val observation = observationSnapshot.getValue(Observation::class.java)
                        if (observation != null && observation.userId == user.uid) {
                            observationArrayList.add(observation)
                        }
                    }
                    val adapter = ObservationAdapter(observationArrayList)
                    observationRecyclerView.adapter = adapter
                    if (observationArrayList.isEmpty()) {
                        if(_binding != null) {
                            binding.noItems.visibility = View.VISIBLE
                        }
                    } else {
                            if(_binding != null){
                            binding.noItems.visibility = View.GONE
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}