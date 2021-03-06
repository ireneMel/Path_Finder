package com.example.pathfinder.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.recreate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pathfinder.customization.ThemeClickListener
import com.example.pathfinder.customization.ThemeManager
import com.example.pathfinder.customization.ThemeStorage.Companion.getThemeColor
import com.example.pathfinder.customization.ThemeStorage.Companion.saveThemeColor
import com.example.pathfinder.customization.recyclerview.DataSource
import com.example.pathfinder.customization.recyclerview.ThemeListAdapter
import com.example.pathfinder.databinding.FragmentCustomizationBinding

class CustomizationFragment : Fragment() {

    private lateinit var themeAdapter: ThemeListAdapter
    private lateinit var binding: FragmentCustomizationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomizationBinding.inflate(layoutInflater, container, false)

        initRecyclerview()

        return binding.root
    }

    private fun initRecyclerview() {
        binding.recyclerviewTheme.apply {
            layoutManager = LinearLayoutManager(requireContext())
            themeAdapter = ThemeListAdapter(themeClickListener, DataSource.createDataSet())
            adapter = themeAdapter
        }
    }
    
    private val themeClickListener = object : ThemeClickListener {
        override fun onChosen(chosenColor: String) {
            if (chosenColor == getThemeColor(requireContext())) {
                Toast.makeText(requireContext(), "Theme has been chosen", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            saveThemeColor(requireContext(), chosenColor)
            ThemeManager.setCustomTheme(requireActivity(), chosenColor)
            recreate(requireActivity())
        }
    }
}