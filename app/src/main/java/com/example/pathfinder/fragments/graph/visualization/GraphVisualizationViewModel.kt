package com.example.pathfinder.fragments.graph.visualization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pathfinder.core.modes.CombineMods
import com.example.pathfinder.core.modes.CombinedMode
import com.example.pathfinder.core.modes.DefaultDrawMode
import com.example.pathfinder.core.modes.DefaultTouchMode
import com.example.pathfinder.core.uiGraph.PlayAlgoUIGraph
import com.example.pathfinder.fragments.graph.GraphProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class State(val uiGraph: PlayAlgoUIGraph, val combinedMode: CombinedMode) {
	abstract class SelectVertex(uiGraph: PlayAlgoUIGraph, combinedMode: CombinedMode) :
		State(uiGraph, combinedMode)
	
	class SelectStartVertex(uiGraph: PlayAlgoUIGraph, combinedMode: CombinedMode) :
		SelectVertex(uiGraph, combinedMode)
	
	class SelectEndVertex(uiGraph: PlayAlgoUIGraph, combinedMode: CombinedMode) :
		SelectVertex(uiGraph, combinedMode)
	
	class ShowStep(uiGraph: PlayAlgoUIGraph, combinedMode: CombinedMode) :
		State(uiGraph, combinedMode)
}

enum class Action {
	SELECT_START, SELECT_END, PLAY, PAUSE, PREVIOUS, NEXT
}

class GraphVisualizationViewModel(
	graphProvider: GraphProvider
) : ViewModel() {
	private val uiGraph = PlayAlgoUIGraph(
		graphProvider.design, graph = graphProvider.graph
	)
	private val _state = MutableStateFlow<State>(
		State.SelectStartVertex(uiGraph, getModes(Action.SELECT_START))
	)
	
	val state = _state.asStateFlow()
	
	private fun getModes(state: Action) = CombineMods(DefaultDrawMode(uiGraph), DefaultTouchMode)
	
	class Factory(
		private val graphProvider: GraphProvider
	) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			return GraphVisualizationViewModel(graphProvider) as T
		}
	}
}