package com.example.pathfinder

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pathfinder.customization.CustomizationFragment
import com.example.pathfinder.customization.ThemeManager
import com.example.pathfinder.customization.ThemeManager.Companion.setCustomTheme
import com.example.pathfinder.customization.ThemeStorage
import com.example.pathfinder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomTheme(this, ThemeStorage.getThemeColor(this).toString())
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createButton.setOnClickListener {

        }

        //load data from the file
        binding.loadButton.setOnClickListener {

        }

        //open new fragment with customization options
        binding.customizeButton.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, CustomizationFragment())
                .addToBackStack(null)
                .commit()
        }

        //manual on how to use the app
        binding.helpButton.setOnClickListener {

        }
    }
}