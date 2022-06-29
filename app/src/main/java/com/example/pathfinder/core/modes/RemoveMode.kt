package com.example.pathfinder.core.modes

import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.uiGraph.EditUIGraph
import com.example.pathfinder.core.uiGraph.FindUIEdge
import com.example.pathfinder.core.uiGraph.FindUIVertex
import com.example.pathfinder.core.uiGraph.UIGraph

class RemoveMode(
	private val findUIVertex: FindUIVertex,
	private val findUIEdge: FindUIEdge,
	private val graph: EditUIGraph
) : TouchMode {
	override fun onTouch(event: MotionEvent): Boolean {
		if (event.action != MotionEvent.ACTION_DOWN) return false
		val vertexId = findUIVertex.findIndex(PointF(event.x, event.y), graph)
		if (vertexId != -1) {
			graph.removeVertex(vertexId)
			return true
		}
		val edge = findUIEdge.findIndex(PointF(event.x, event.y), graph)
		if (edge.from != -1) {
			graph.removeEdge(edge)
			return true
		}
		return true
	}
}