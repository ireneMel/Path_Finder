package com.example.pathfinder.fragments.graph.visualization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pathfinder.core.algorithms.Dijkstra
import com.example.pathfinder.core.algorithms.GraphStep
import com.example.pathfinder.core.modes.*
import com.example.pathfinder.core.uiGraph.PlayAlgoUIGraph
import com.example.pathfinder.core.uiGraph.finders.FindUIVertex
import com.example.pathfinder.fragments.graph.GraphProvider
import com.example.pathfinder.fragments.graph.visualization.Action.*
import com.example.pathfinder.models.Vertex
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
	SELECT_START, SELECT_END, PLAY, PAUSE, PREVIOUS, NEXT, CLEAR
}

class GraphVisualizationViewModel(
	private val graphProvider: GraphProvider
) : ViewModel() {
	private val findUIVertex = FindUIVertex(graphProvider.design.vertexDesign.radius * 2)
	private val uiGraph = PlayAlgoUIGraph(
		graphProvider.design, graph = graphProvider.graph
	)
	private val design = graphProvider.algoDesign
	private val _state = MutableStateFlow<State>(
		State.SelectStartVertex(uiGraph, getModes(SELECT_START))
	)
	
	private var startVertex = -1
	private var endVertex = -1
	
	val state = _state.asStateFlow()
	
	fun setAction(action: Action) {
		when (action) {
			PLAY     -> play()
			PAUSE    -> pause()
			PREVIOUS -> prevStep()
			NEXT     -> nextStep()
			CLEAR    -> clear()
			else     -> {}
		}
		when (action) {
			CLEAR, SELECT_START -> _state.value = State.SelectStartVertex(uiGraph, getModes(action))
			SELECT_END          -> _state.value = State.SelectEndVertex(uiGraph, getModes(action))
			PREVIOUS, NEXT      -> {
				uiGraph.resetGraphPaint()
				pause()
				uiGraph.setGraphStep(generatedSteps[currentStep], design)
				_state.value = State.ShowStep(uiGraph, getModes(action))
			}
			else                -> {}
		}
	}
	
	private fun getModes(state: Action) = when (state) {
		SELECT_START                       -> CombineMods(
			DefaultDrawMode(uiGraph), SelectVertexMode(findUIVertex, uiGraph, ::selectStartVertex)
		)
		SELECT_END                         -> CombineMods(
			DefaultDrawMode(uiGraph), SelectVertexMode(findUIVertex, uiGraph, ::selectEndVertex)
		)
		PLAY, PAUSE, PREVIOUS, NEXT, CLEAR -> CombineMods(
			DefaultDrawMode(uiGraph), DefaultTouchMode
		)
	}
	
	private fun selectStartVertex(id: Int) {
		if (startVertex != -1) uiGraph.resetVertex(startVertex)
		startVertex = id
		uiGraph.setVertexStart(id, design.startDesign)
	}
	
	private fun selectEndVertex(id: Int) {
		if (endVertex != -1) uiGraph.resetVertex(endVertex)
		endVertex = id
		uiGraph.setVertexEnd(id, design.endDesign)
	}
	
	private var generatedSteps = emptyList<GraphStep>()
	private var currentStep = -1
	private var playJob: Job? = null
	
	private fun setup(): Boolean {
		if (startVertex == -1 || endVertex == -1) return false
		if (currentStep == -1) generatedSteps =
			Dijkstra(startVertex, endVertex, graphProvider.graph).generate()
		return true
	}
	
	val isPlaying get() = playJob != null
	
	private fun play() {
		if (currentStep == -1) setup()
		playJob = viewModelScope.launch {
			while (true) {
				uiGraph.setGraphStep(nextStep() ?: break, design)
				_state.value = State.ShowStep(uiGraph, getModes(PLAY))
				delay(1000)
			}
		}
	}
	
	private fun pause() {
		playJob?.cancel()
		playJob = null
	}
	
	private fun clear() {
		startVertex = -1
		endVertex = -1
		currentStep = -1
		uiGraph.resetGraphPaint()
		_state.value = State.SelectStartVertex(uiGraph, getModes(SELECT_START))
	}
	
	private fun nextStep(): GraphStep? {
		if (currentStep == generatedSteps.lastIndex) return null
		return generatedSteps[++currentStep]
	}
	
	private fun prevStep(): GraphStep? {
		if (currentStep == 0) return null
		return generatedSteps[--currentStep];
	}
	
	class Factory(
		private val graphProvider: GraphProvider
	) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			return GraphVisualizationViewModel(graphProvider) as T
		}
	}
}