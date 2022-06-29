package com.example.pathfinder.creation

import android.graphics.Canvas
import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pathfinder.core.modes.*
import com.example.pathfinder.core.uiGraph.EditUIGraph
import com.example.pathfinder.core.uiGraph.GraphDesign
import com.example.pathfinder.core.uiGraph.PlayAlgoUIGraph
import com.example.pathfinder.core.uiGraph.finders.FindUIEdge
import com.example.pathfinder.core.uiGraph.finders.FindUIVertex
import com.example.pathfinder.creation.EditState.*
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class State(val combinedMode: CombinedMode) {
	class Edit(val uiGraph: EditUIGraph, combinedMode: CombinedMode) : State(combinedMode)
	abstract class SetPrice(val editState: Edit): State(editState.combinedMode)
	class SetVertexPrice(val vertexId: Int, editState: Edit): SetPrice(editState)
	class SetEdgePrice(val edge: Edge, editState: Edit) : SetPrice(editState)
	class Visualize(val uiGraph: PlayAlgoUIGraph, combinedMode: CombinedMode) : State(combinedMode)
}

enum class EditState {
	DEFAULT, ADD_VERTEX, ADD_EDGE, REMOVE, SET
}

class GraphCreationViewModel(design: GraphDesign) : ViewModel() {
	private val editUIGraph = EditUIGraph(
		design, graph = Graph()
	)
	private val vertexFinder = FindUIVertex(design.vertexDesign.radius * 2)
	private val edgeFinder = FindUIEdge(design.vertexDesign.radius)
	
	private val _state = MutableStateFlow<State>(
		State.Edit(editUIGraph, DefaultMode(editUIGraph))
	)
	
	val state = _state.asStateFlow()
	
	private class CombineMods(private val drawMode: DrawMode, private val touchMode: TouchMode) :
		CombinedMode {
		override fun onDraw(canvas: Canvas) {
			drawMode.onDraw(canvas)
		}
		
		override fun onTouch(event: MotionEvent): Boolean {
			return touchMode.onTouch(event)
		}
	}
	
	private fun getModes(state: EditState) = when (state) {
		DEFAULT -> DefaultMode(editUIGraph)
		ADD_VERTEX -> CombineMods(DefaultDrawMode(editUIGraph), AddVertexMode(editUIGraph))
		ADD_EDGE -> AddEdgeMode(vertexFinder, editUIGraph)
		REMOVE -> CombineMods(
			DefaultDrawMode(editUIGraph),
			RemoveMode(vertexFinder, edgeFinder, editUIGraph)
		)
		SET -> CombineMods(
			DefaultDrawMode(editUIGraph),
			SetPriceMode(vertexFinder, edgeFinder, editUIGraph,::onVertexSet, ::onEdgeSet)
		)
	}
	
	fun setEditState(state: EditState) {
		_state.value = State.Edit(editUIGraph, getModes(state))
	}
	
	private fun onVertexSet(id: Int) {
		_state.value = State.SetVertexPrice(id, _state.value as State.Edit)
	}
	
	private fun onEdgeSet(edge: Edge) {
		_state.value = State.SetEdgePrice(edge, _state.value as State.Edit)
	}
	
	fun setPrice(price: Float){
		val state = _state.value
		if (state !is State.SetPrice) return
		val editState = state.editState
		if (state is State.SetVertexPrice){
			editState.uiGraph.setVertexCost(state.vertexId, price)
		}
		if (state is State.SetEdgePrice){
			state.edge.cost = price
			editState.uiGraph.setEdgeCost(state.edge)
		}
		_state.value = editState
	}
	
	fun loadGraph(graph: Graph){
		editUIGraph.graph = graph
		val state = _state.value
		_state.compareAndSet(state, state)
	}
	
	class Factory(private val design: GraphDesign) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			return GraphCreationViewModel(design) as T
		}
	}
}