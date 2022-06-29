package com.example.pathfinder

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.codertainment.materialintro.MaterialIntroConfiguration
import com.codertainment.materialintro.sequence.SkipLocation
import com.codertainment.materialintro.shape.ShapeType
import com.codertainment.materialintro.utils.materialIntroSequence
import com.example.pathfinder.creation.GraphCreationFragment
import com.example.pathfinder.customization.CustomizationFragment
import com.example.pathfinder.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var binding: FragmentMainBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)

        setHasOptionsMenu(true)

        binding.createButton.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.container, GraphCreationFragment.newInstance(false))
                addToBackStack(null)
            }
        }

        //load data from the file
        binding.loadButton.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.container, GraphCreationFragment.newInstance(true))
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

    private fun MaterialIntroConfiguration.basicConfig(mTargetView: View, message: String) {
        isDotViewEnabled = true
        isDotAnimationEnabled = true
        infoCustomView = TextView(requireActivity()).apply {
            text = message
        }
        infoTextAlignment = View.TEXT_ALIGNMENT_CENTER
        targetView = mTargetView
        showOnlyOnce = false
        shapeType = ShapeType.CIRCLE
        skipLocation = SkipLocation.TOP_RIGHT
        userClickAsDisplayed = true
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
                        basicConfig(binding.createButton, "You can create a new graph")
                    }

                    addConfig {
                        basicConfig(binding.loadButton, "You can load a graph from a file")
                    }

                    addConfig {
                        basicConfig(binding.customizeButton, "You can customize Path Finder")
                    }
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}