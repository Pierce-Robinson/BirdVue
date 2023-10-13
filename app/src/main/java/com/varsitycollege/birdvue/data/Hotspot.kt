package com.varsitycollege.birdvue.data

data class Hotspot(
    val locId: String,
    val locName: String,
    val countryCode: String,
    val subnational1Code: String,
    val lat: Double,
    val lng: Double,
    val latestObsDt: String,
    val numSpeciesAllTime: Int
)
