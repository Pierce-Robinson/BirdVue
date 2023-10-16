package com.varsitycollege.birdvue.data

data class Observation(
    val id: String ?= null,
    val birdName: String ?= null,
    val date: String ?= null,
    val photo: String ?= null,
    val details: String ?= null,
    val lat: Number ?= null,
    val lng: Number ?= null,
    val location: String ?= null,
    val likes: Number ?= null,
    val comments: List<Comment> ?= null,
    val userId: String ?= null
)

