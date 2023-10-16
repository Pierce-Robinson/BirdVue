package com.varsitycollege.birdvue.data

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val currentFragment = MutableLiveData<Fragment>()
    private val hotspotList = MutableLiveData<List<Hotspot>>(emptyList())

    fun setCurrentFragment(fragment: Fragment) {
        currentFragment.value = fragment
    }

    fun getCurrentFragment() : Fragment? {
        return currentFragment.value
    }

    // Add a method to update hotspotList as needed
    fun updateHotspotList(newList: List<Hotspot>) {
        hotspotList.value = newList
    }

    fun getHotspotList(): List<Hotspot> {
        return hotspotList.value ?: emptyList()
    }

}