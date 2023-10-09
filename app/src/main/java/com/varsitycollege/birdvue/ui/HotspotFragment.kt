package com.varsitycollege.birdvue.ui

import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.varsitycollege.birdvue.BuildConfig
import com.varsitycollege.birdvue.api.EBirdAPI
import com.varsitycollege.birdvue.data.Hotspot
import com.varsitycollege.birdvue.databinding.FragmentHotspotBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HotspotFragment : Fragment() {

    private var _binding: FragmentHotspotBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentHotspotBinding.inflate(inflater, container, false)

        //Call api to fetch session token for unique questions
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.ebird.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(EBirdAPI::class.java)
        api.getHotspots(-25.74, 28.22, "json", 10, BuildConfig.EBIRD_API_KEY).enqueue(object : Callback<List<Hotspot>> {
            override fun onResponse(call: Call<List<Hotspot>>, response: Response<List<Hotspot>>) {
                val hotspotData = response.body()
                var text = ""
                if (hotspotData != null) {
                    for (h in hotspotData) {
                        text += h.locName + "\n"
                    }
                }
                binding.apiTest.text = text
            }

            override fun onFailure(call: Call<List<Hotspot>>, t: Throwable) {
                Log.e("Token error", t.toString())
            }

        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}