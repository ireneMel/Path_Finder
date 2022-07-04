package com.example.pathfinder.fragments.graph.creation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pathfinder.core.modes.*
import com.example.pathfinder.core.uiGraph.BiEditUIGraph
import com.example.pathfinder.core.uiGraph.EditUIGraph
import com.example.pathfinder.core.uiGraph.OneEditUIGraph
import com.example.pathfinder.core.uiGraph.finders.FindUIEdge
import com.example.pathfinder.core.uiGraph.finders.FindUIVertex
import com.example.pathfinder.fragments.graph.GraphProvider
import com.example.pathfinder.fragments.graph.creation.Action.*
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class State(val touchMode: TouchMode, val drawMode: DrawMode) {
	class Edit(val uiGraph: EditUIGraph, touchMode: TouchMode, drawMode: DrawMode) :
		State(touchMode, drawMode)
	
	abstract class SetPrice(val editState: Edit) : State(editState.touchMode, editState.drawMode)
	class SetVertexPrice(val vertexId: Int, editState: Edit) : SetPrice(editState)
	class SetEdgePrice(val edge: Edge, editState: Edit) : SetPrice(editState)
}

enum class Action {
	DEFAULT, ADD_VERTEX, ADD_EDGE, REMOVE, SET
}

class GraphCreationViewModel(private var graphProvider: GraphProvider, isBiGraph: Boolean) :
	ViewModel() {
	
	private val uiGraph = if (isBiGraph) BiEditUIGraph(
		graphProvider.design, graph = graphProvider.graph
	) else OneEditUIGraph(
		graphProvider.design, graph = graphProvider.graph
	)
	private val vertexFinder = FindUIVertex(graphProvider.design.vertexDesign.radius * 2)
	private val edgeFinder = FindUIEdge(graphProvider.design.vertexDesign.radius)
	private val _state = MutableStateFlow<State>(
		State.Edit(uiGraph, DefaultTouchMode, DefaultDrawMode(uiGraph))
	)
	
	val state = _state.asStateFlow()
	
	fun setEditState(state: Action) {
		Log.d("Debug141", "setEditState: ")
		_state.value = when (state) {
			DEFAULT    -> State.Edit(uiGraph, DefaultTouchMode, DefaultDrawMode(uiGraph))
			ADD_VERTEX -> State.Edit(uiGraph, AddVertexMode(uiGraph), DefaultDrawMode(uiGraph))
			ADD_EDGE   -> {
				val mode = AddEdgeMode(vertexFinder, uiGraph)
				State.Edit(uiGraph, mode, mode)
			}
			REMOVE     -> State.Edit(
				uiGraph,
				RemoveMode(vertexFinder, edgeFinder, uiGraph),
				DefaultDrawMode(uiGraph)
			)
			SET        -> State.Edit(
				uiGraph,
				SetPriceMode(
					vertexFinder,
					edgeFinder,
					uiGraph,
					::onVertexSet,
					::onEdgeSet
				),
				DefaultDrawMode(uiGraph)
			)
		}
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
		graphProvider.graph = graph
		val state = _state.value
		_state.compareAndSet(state, state)
	}
	
	class Factory(private val graphProvider: GraphProvider, private val isBiGraph: Boolean) :
		ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			return GraphCreationViewModel(graphProvider, isBiGraph) as T
		}
	}
}