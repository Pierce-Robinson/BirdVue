package com.varsitycollege.birdvue.data

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val currentFragment = MutableLiveData<Fragment>()

    fun setCurrentFragment(fragment: Fragment) {
        currentFragment.value = fragment
    }

    fun getCurrentFragment() : Fragment? {
        return currentFragment.value
    }

}