package com.example.pathfinder.fragments.graph.visualization

import android.os.Bundle
import android.view.View
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
		lifecycleScope.launchWhenStarted {
			viewModel.state.collect(::bindState)
		}
	}
	
	private fun bindState(state: State){}
}