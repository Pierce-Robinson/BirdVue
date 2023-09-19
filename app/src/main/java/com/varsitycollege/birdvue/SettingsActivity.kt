package com.varsitycollege.birdvue

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val darkModeCheckbox = findViewById<CheckBox>(R.id.darkModeCheckbox)
        darkModeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            // haandle the dark mode setting here
        }


    }
}