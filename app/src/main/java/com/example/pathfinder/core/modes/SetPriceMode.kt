package com.example.pathfinder.core.modes

import android.view.MotionEvent
import com.example.pathfinder.core.uiGraph.EditUIGraph
import com.example.pathfinder.core.uiGraph.finders.FindUIEdge
import com.example.pathfinder.core.uiGraph.finders.FindUIVertex
import com.example.pathfinder.models.Edge

class SetPriceMode(
	findUIVertex: FindUIVertex,
	findUIEdge: FindUIEdge,
	graph: EditUIGraph,
	onVertexSet: (vertexId: Int) -> Unit,
	onEdgeSet: (edge: Edge) -> Unit,
) : TouchMode {
	private val selectEdgeMode = SelectEdgeMode(findUIEdge, graph, onEdgeSet)
	private val selectVertexMode = SelectVertexMode(findUIVertex, graph, onVertexSet)
	override fun onTouch(event: MotionEvent): Boolean {
		if (selectVertexMode.onTouch(event)) return true
		if (selectEdgeMode.onTouch(event)) return true
		return false
	}
}