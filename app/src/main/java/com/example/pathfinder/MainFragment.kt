package com.example.pathfinder

import android.os.Bundle
import android.text.TextUtils.replace
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.pathfinder.core.serialization.read.ReadGraphFromFile
import com.example.pathfinder.core.serialization.read.ReadState
import com.example.pathfinder.customization.CustomizationFragment
import com.example.pathfinder.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var binding: FragmentMainBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)

        binding.createButton.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.container, GraphCreationFragment())
                addToBackStack(null)
            }
        }

        //load data from the file
        binding.loadButton.setOnClickListener {

        }

        //open new fragment with customization options
        binding.customizeButton.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.container, CustomizationFragment())
                addToBackStack(null)
            }
        }

    }

    private val graphReader: ReadGraphFromFile by lazy {
        ReadGraphFromFile(
            requireActivity().activityResultRegistry,
            viewLifecycleOwner,
            requireActivity().contentResolver
        )
    }

    private fun makeToast(text: String) =
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
}