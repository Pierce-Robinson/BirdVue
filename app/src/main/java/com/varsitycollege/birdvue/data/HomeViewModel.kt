package com.varsitycollege.birdvue.data

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val currentFragment = MutableLiveData<Fragment>()

    //https://developer.android.com/topic/libraries/architecture/livedata
    //Accessed 18 October 2023
    val hotspotList: MutableLiveData<List<Hotspot>> by lazy {
        MutableLiveData<List<Hotspot>>()
    }

    val observationList: MutableLiveData<List<Observation>> by lazy {
        MutableLiveData<List<Observation>>()
    }

    val currentDistance: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    val metric: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun setCurrentFragment(fragment: Fragment) {
        currentFragment.value = fragment
    }

    fun getCurrentFragment() : Fragment? {
        return currentFragment.value
    }

}