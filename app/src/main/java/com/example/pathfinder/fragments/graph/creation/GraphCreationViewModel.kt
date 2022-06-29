package com.example.pathfinder.fragments.graph.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pathfinder.core.modes.*
import com.example.pathfinder.core.uiGraph.EditUIGraph
import com.example.pathfinder.core.uiGraph.finders.FindUIEdge
import com.example.pathfinder.core.uiGraph.finders.FindUIVertex
import com.example.pathfinder.fragments.graph.GraphProvider
import com.example.pathfinder.fragments.graph.creation.Action.*
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class State(val combinedMode: CombinedMode) {
	class Edit(val uiGraph: EditUIGraph, combinedMode: CombinedMode) : State(combinedMode)
	abstract class SetPrice(val editState: Edit) : State(editState.combinedMode)
	class SetVertexPrice(val vertexId: Int, editState: Edit) : SetPrice(editState)
	class SetEdgePrice(val edge: Edge, editState: Edit) : SetPrice(editState)
}

enum class Action {
	DEFAULT, ADD_VERTEX, ADD_EDGE, REMOVE, SET
}

class GraphCreationViewModel(graphProvider: GraphProvider) : ViewModel() {
	private val uiGraph = EditUIGraph(
		graphProvider.design, graph = graphProvider.graph
	)
	private val vertexFinder = FindUIVertex(graphProvider.design.vertexDesign.radius * 2)
	private val edgeFinder = FindUIEdge(graphProvider.design.vertexDesign.radius)
	private val _state = MutableStateFlow<State>(
		State.Edit(uiGraph, DefaultMode(uiGraph))
	)
	
	val state = _state.asStateFlow()
	
	private fun getModes(state: Action) = when (state) {
		DEFAULT    -> DefaultMode(uiGraph)
		ADD_VERTEX -> CombineMods(DefaultDrawMode(uiGraph), AddVertexMode(uiGraph))
		ADD_EDGE   -> AddEdgeMode(vertexFinder, uiGraph)
		REMOVE     -> CombineMods(
			DefaultDrawMode(uiGraph), RemoveMode(vertexFinder, edgeFinder, uiGraph)
		)
		SET        -> CombineMods(
			DefaultDrawMode(uiGraph),
			SetPriceMode(vertexFinder, edgeFinder, uiGraph, ::onVertexSet, ::onEdgeSet)
		)
	}
	
	fun setEditState(state: Action) {
		_state.value = State.Edit(uiGraph, getModes(state))
	}
	
	private fun onVertexSet(id: Int) {
		_state.value = State.SetVertexPrice(id, _state.value as State.Edit)
	}
	
	private fun onEdgeSet(edge: Edge) {
		_state.value = State.SetEdgePrice(edge, _state.value as State.Edit)
	}
	
	fun setPrice(price: Float) {
		val state = _state.value
		if (state !is State.SetPrice) return
		val editState = state.editState
		if (state is State.SetVertexPrice) {
			editState.uiGraph.setVertexCost(state.vertexId, price)
		}
		if (state is State.SetEdgePrice) {
			state.edge.cost = price
			editState.uiGraph.setEdgeCost(state.edge)
		}
		_state.value = editState
	}
	
	fun loadGraph(graph: Graph) {
		uiGraph.graph = graph
		val state = _state.value
		_state.compareAndSet(state, state)
	}
	
	class Factory(private val graphProvider: GraphProvider) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			return GraphCreationViewModel(graphProvider) as T
		}
	}
}