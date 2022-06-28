package com.example.pathfinder.core.modes

import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.FindUIEdge
import com.example.pathfinder.core.FindUIVertex
import com.example.pathfinder.core.UIGraph
import com.example.pathfinder.models.Edge

class SetPriceMode(
	private val findUIVertex: FindUIVertex,
	private val findUIEdge: FindUIEdge,
	private val onVertexSet: (vertexId: Int) -> Unit,
	private val onEdgeSet: (edge: Edge) -> Unit,
) : TouchMode {
	override fun onTouch(event: MotionEvent, graph: UIGraph): Boolean {
		if (event.action != MotionEvent.ACTION_DOWN) return false
		val vertexId = findUIVertex.findIndex(PointF(event.x, event.y), graph)
		if (vertexId != -1) {
			onVertexSet(vertexId)
			return true
		}
		val edge = findUIEdge.findIndex(PointF(event.x, event.y), graph)
		if (edge.from != -1) {
			onEdgeSet(edge)
			return true
		}
		return false
	}
}