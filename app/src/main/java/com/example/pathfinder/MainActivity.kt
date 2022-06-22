package com.example.pathfinder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pathfinder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding.createButton.setOnClickListener {

        }

        //load data from the file
        binding.loadButton.setOnClickListener {

        }

        //open new fragment with customization options
        binding.customizeButton.setOnClickListener {

        }

        //manual on how to use the app
        binding.helpButton.setOnClickListener {

        }
    }
}