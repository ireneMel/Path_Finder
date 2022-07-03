package com.example.pathfinder.core.modes

import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.uiGraph.EditUIGraph
import com.example.pathfinder.core.uiGraph.UIGraph
import com.example.pathfinder.core.uiGraph.finders.FindUIEdge
import com.example.pathfinder.models.Edge

class SelectEdgeMode(
	private val findUIEdge: FindUIEdge,
	private val graph: UIGraph,
	private val onEdgeSet: (edge: Edge) -> Unit
) : TouchMode {
	override fun onTouch(event: MotionEvent): Boolean {
		if (event.action != MotionEvent.ACTION_DOWN) return false
		val edge = findUIEdge.findIndex(PointF(event.x, event.y), graph)
		if (edge.from != -1) {
			onEdgeSet(edge)
			return true
		}
		return false
	}
}