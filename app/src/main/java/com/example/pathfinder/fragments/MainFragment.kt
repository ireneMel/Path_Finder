package com.example.pathfinder.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.codertainment.materialintro.utils.materialIntroSequence
import com.example.pathfinder.R
import com.example.pathfinder.databinding.FragmentMainBinding
import com.example.pathfinder.fragments.graph.GraphFragment
import com.example.pathfinder.utils.Hints.basicConfig

class MainFragment : Fragment(R.layout.fragment_main) {
	private lateinit var binding: FragmentMainBinding
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentMainBinding.bind(view)
		
		setHasOptionsMenu(true)
		
		binding.createOneButton.setOnClickListener {
			parentFragmentManager.commit {
				replace(
					R.id.container, GraphFragment.newInstance(
						isOpenClicked = false, isBiDirectional = false
					)
				)
				addToBackStack(null)
			}
		}
		
		binding.createBiButton.setOnClickListener {
			parentFragmentManager.commit {
				replace(
					R.id.container, GraphFragment.newInstance(
						isOpenClicked = false, isBiDirectional = true
					)
				)
				addToBackStack(null)
			}
		}
		
		//load data from the file
		binding.loadButton.setOnClickListener {
			parentFragmentManager.commit {
				replace(
					R.id.container, GraphFragment.newInstance(
						isOpenClicked = true, isBiDirectional = true
					)
				)
				addToBackStack(null)
			}
		}
		
		//open new fragment with customization options
		binding.customizeButton.setOnClickListener {
			parentFragmentManager.commit {
				replace(R.id.container, CustomizationFragment())
				addToBackStack(null)
			}
		}
	}
	
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.menu, menu)
		super.onCreateOptionsMenu(menu, inflater)
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.help -> {
				materialIntroSequence(200, showSkip = true) {
					addConfig {
						basicConfig(binding.createOneButton, "You can create a one direction graph")
					}
					
					addConfig {
						basicConfig(binding.createBiButton, "You can create a two direction graph")
					}
					
					addConfig {
						basicConfig(binding.loadButton, "You can load a graph from a file")
					}
					
					addConfig {
						basicConfig(
							binding.customizeButton, "You can customize Path Finder"
						)
					}
				}
				return true
			}
			else      -> return super.onOptionsItemSelected(item)
		}
	}
}