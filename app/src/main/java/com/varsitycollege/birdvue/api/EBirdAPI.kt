package com.varsitycollege.birdvue.api

import com.varsitycollege.birdvue.data.Hotspot
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EBirdAPI{
    //Base URL: https://api.ebird.org/v2/
    //Get nearby hotspots
    @GET("ref/hotspot/geo")
    fun getHotspots(@Query("lat") lat: Number, @Query("lng") lng: Number, @Query("fmt") fmt: String, @Query("dst") dst: Number, @Query("key") key: String): Call<List<Hotspot>>
}
