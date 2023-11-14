package com.varsitycollege.birdvue.ui

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varsitycollege.birdvue.data.Observation
import com.varsitycollege.birdvue.data.ObservationAdapterCom
import com.varsitycollege.birdvue.databinding.FragmentCommunityBinding
import java.util.Locale
import java.util.Date
import java.text.ParseException



class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var observationComArrayList: ArrayList<Observation>
    private lateinit var observationComRecyclerView: RecyclerView
    //search
    private lateinit var searchView: SearchView
    private lateinit var adapter: ObservationAdapterCom

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        observationComRecyclerView = binding.recyclerViewCom
        observationComArrayList = arrayListOf()


        observationComRecyclerView.layoutManager = LinearLayoutManager(context)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            getData(currentUser)
        }
        // Set up the SearchView
        setupSearchView()

        return binding.root
    }
    private fun setupSearchView() {
        searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterObservations(newText ?: "")
                return true
            }
        })
    }

    //Search for post by date or by birdname
    private fun filterObservations(query: String) {
        val filteredList = if (query.isEmpty()) {
            observationComArrayList
        } else {
            observationComArrayList.filter { observation ->
                observation.date!!.contains(query, ignoreCase = true) ||
                        observation.birdName!!.contains(query, ignoreCase = true)
            }
        }
        updateRecyclerView(filteredList)
    }

    //update list based on filter
    private fun updateRecyclerView(list: List<Observation>) {
        adapter = ObservationAdapterCom(list)
        observationComRecyclerView.adapter = adapter
    }
    private fun getData(user: FirebaseUser) {
        database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
        ref = database.getReference("observations")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // we clear old dtaa
                    observationComArrayList.clear()
                    for (observationSnapshot in snapshot.children) {
                        val observation = observationSnapshot.getValue(Observation::class.java)
                        if (observation != null) {
                            observationComArrayList.add(observation)
                        }
                    }

                    // sort array by date in descending order here
                    observationComArrayList.sortByDescending { observation ->
                        parseDate(observation.date ?: "")
                    }


                    val adapter = ObservationAdapterCom(observationComArrayList)
                    observationComRecyclerView.adapter = adapter

                    if (observationComArrayList.isEmpty()) {
                        if (_binding != null) {
                            binding.noItems.visibility = View.VISIBLE
                        }
                    } else {
                        if (_binding != null) {
                            binding.noItems.visibility = View.GONE
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(context, "There was an error during data retrieval: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseDate(dateString: String): Date {
        val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return try {
            format.parse(dateString) ?: Date(0)
        } catch (e: ParseException) {
            e.printStackTrace()
            Date(0)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}