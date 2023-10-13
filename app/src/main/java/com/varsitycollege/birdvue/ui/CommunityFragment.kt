package com.varsitycollege.birdvue.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.birdvue.R
import com.varsitycollege.birdvue.data.Hotspot
import com.varsitycollege.birdvue.data.HotspotAdapter
import com.varsitycollege.birdvue.databinding.FragmentCommunityBinding
import com.varsitycollege.birdvue.databinding.FragmentHotspotBinding

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var hotspotData: List<Hotspot>

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}