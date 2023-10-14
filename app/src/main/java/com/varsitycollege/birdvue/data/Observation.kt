package com.varsitycollege.birdvue.data

data class Observation(
    val id: String,
    val birdName: String,
    val date: String,
    val photo: String ?= null,
    val details: String,
    val lat: Number,
    val lng: Number,
    val location: String,
    val likes: Number,
    val comments: List<Comment>,
    val userId: String ?= null
)

