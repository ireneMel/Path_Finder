package com.example.pathfinder.fragments.graph.visualization

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.pathfinder.R
import com.example.pathfinder.databinding.FragmentGraphVisualizationBinding
import com.example.pathfinder.fragments.graph.GraphFragment
import kotlinx.coroutines.flow.collect

class GraphVisualizationFragment : Fragment(R.layout.fragment_graph_visualization) {
	private lateinit var binding: FragmentGraphVisualizationBinding
	private val viewModel: GraphVisualizationViewModel by viewModels {
		GraphVisualizationViewModel.Factory((requireParentFragment() as GraphFragment).viewModel)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentGraphVisualizationBinding.bind(view)
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
			parentFragmentManager.popBackStack()
		}
		bindStable()
		lifecycleScope.launchWhenStarted {
			viewModel.state.collect(::bindState)
		}
	}
	
	private fun bindStable() = with(binding) {
		selectStart.setOnClickListener { viewModel.setAction(Action.SELECT_START) }
		selectEnd.setOnClickListener { viewModel.setAction(Action.SELECT_END) }
		playPause.setOnClickListener {
			viewModel.setAction(if (viewModel.isPlaying) Action.PAUSE else Action.PLAY)
		}
		previous.setOnClickListener { viewModel.setAction(Action.PREVIOUS) }
		next.setOnClickListener { viewModel.setAction(Action.NEXT) }
		clear.setOnClickListener { viewModel.setAction(Action.CLEAR) }
	}
	
	private fun bindState(state: State) = with(binding) {
		val isSelecting = (state is State.SelectVertex)
		
		playPause.text = if(viewModel.isPlaying) "Pause" else "Play"
		
		selectStart.isVisible = isSelecting
		selectEnd.isVisible = isSelecting
		previous.isVisible = !isSelecting
		next.isVisible = !isSelecting
		textInfo.isVisible = !isSelecting
		
		if(!isSelecting)
			textInfo.text = "Shortest path cost is ${(state as State.ShowStep).cost}"
		
		graph.graph = state.uiGraph
		graph.touchMode = state.combinedMode
		graph.drawMode = state.combinedMode
	}
}