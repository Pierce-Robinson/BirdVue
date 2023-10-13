package com.varsitycollege.birdvue.data

data class Hotspot(
    val locId: String ?= null,
    val locName: String ?= null,
    val countryCode: String ?= null,
    val subnational1Code: String ?= null,
    val lat: Double ?= null,
    val lng: Double ?= null,
    val latestObsDt: String ?= null,
    val numSpeciesAllTime: Int ?= null
)
