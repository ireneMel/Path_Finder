package com.example.pathfinder

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.pathfinder.customization.CustomizationFragment
import com.example.pathfinder.customization.ThemeManager
import com.example.pathfinder.customization.ThemeStorage
import com.example.pathfinder.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {
	private lateinit var binding: FragmentMainBinding
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentMainBinding.bind(view)
		
		binding.createButton.setOnClickListener {
		
		}
		
		//load data from the file
		binding.loadButton.setOnClickListener {
		
		}
		
		//open new fragment with customization options
		binding.customizeButton.setOnClickListener {
			parentFragmentManager.commit {
				add(R.id.container, CustomizationFragment())
				addToBackStack(null)
			}
		}
		
		//manual on how to use the app
		binding.helpButton.setOnClickListener {
		
		}
	}
}