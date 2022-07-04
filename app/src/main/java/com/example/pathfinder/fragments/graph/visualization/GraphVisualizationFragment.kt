package com.example.pathfinder.fragments.graph.visualization

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.codertainment.materialintro.utils.materialIntroSequence
import com.example.pathfinder.R
import com.example.pathfinder.customization.ThemeStorage.getThemeColor
import com.example.pathfinder.databinding.FragmentGraphVisualizationBinding
import com.example.pathfinder.fragments.graph.GraphFragment
import com.example.pathfinder.utils.Hints.basicConfig
import com.example.pathfinder.utils.getThemeColor
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.flow.collect

class GraphVisualizationFragment : Fragment(R.layout.fragment_graph_visualization) {
    private lateinit var binding: FragmentGraphVisualizationBinding
    private val viewModel: GraphVisualizationViewModel by viewModels {
        GraphVisualizationViewModel.Factory((requireParentFragment() as GraphFragment).viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGraphVisualizationBinding.bind(view)

        setHasOptionsMenu(true)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }
        bindStable()
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect(::bindState)
        }
    }

    private fun bindStable() = with(binding) {
        selectStart.setOnClickListener {
            viewModel.setAction(Action.SELECT_START)
        }
        selectEnd.setOnClickListener {
            viewModel.setAction(Action.SELECT_END)
        }
        playPause.setOnClickListener {
            viewModel.setAction(if (viewModel.isPlaying) Action.PAUSE else Action.PLAY)
        }
        previous.setOnClickListener { viewModel.setAction(Action.PREVIOUS) }
        next.setOnClickListener { viewModel.setAction(Action.NEXT) }
        clear.setOnClickListener { viewModel.setAction(Action.CLEAR) }
    }

    private fun bindState(state: State) = with(binding) {
        val isSelecting = (state is State.SelectVertex)
        if(isSelecting){
            if(state is State.SelectStartVertex)
                setSelected(selectStart)
            else
                setSelected(selectEnd)
        } else {
            unselect(selectStart)
            unselect(selectEnd)
        }

        playPause.text = if (viewModel.isPlaying) "Pause" else "Run"

        selectStart.isVisible = isSelecting
        selectEnd.isVisible = isSelecting
        previous.isVisible = !isSelecting
        next.isVisible = !isSelecting
        textInfo.isVisible = !isSelecting

        if (!isSelecting)
            textInfo.text = "Shortest path cost is ${(state as State.ShowStep).cost}"

        graph.graph = state.uiGraph
        graph.touchMode = state.combinedMode
        graph.drawMode = state.combinedMode
    }
    
    private fun setSelected(button: MaterialTextView) = with(binding){
        unselect(selectStart)
        unselect(selectEnd)
        button.backgroundTintList =
            ColorStateList.valueOf(getThemeColor(com.google.android.material.R.attr.colorSecondary))
    }
    
    private fun unselect(button: MaterialTextView){
        button.backgroundTintList =
            ColorStateList.valueOf(getThemeColor(com.google.android.material.R.attr.colorPrimary))
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
                        basicConfig(
                            binding.selectStart,
                            "Enter the mode to select the starting point for the algorithm. \nChoose one point from your graph or choose another mode."
                        )
                    }
                    addConfig {
                        basicConfig(
                            binding.selectEnd,
                            "Enter the mode to select the ending point for the algorithm. \nChoose one point from your graph or choose another mode."
                        )
                    }
                    addConfig {
                        basicConfig(
                            binding.playPause,
                            "After setting starting and ending points click play button to start visualizing the best path. If you want the visualizing process to stop also click this button."
                        )
                    }
                    addConfig {
                        basicConfig(binding.clear, "Clear the starting and the ending point")
                    }
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}