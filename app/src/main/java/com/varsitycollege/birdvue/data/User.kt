package com.varsitycollege.birdvue.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val id: String ?= null,
    val username: String ?= null,
    val email: String ?= null,
    val maxDistance: Int ?= null,
    val metricUnits: Boolean ?= null

    //todo: when user checks metric box, display units to user when they click
)
