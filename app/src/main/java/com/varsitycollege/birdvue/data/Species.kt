package com.varsitycollege.birdvue.data

data class Species(
    val id: String,
    val birdName: String,
    val date: String,
    val photo: String,
    val details: String,
    val lat: Number,
    val lng: Number,
    val location: String,
    val likes: Number,
    val comments: List<Comment>
)
