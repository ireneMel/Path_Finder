package com.example.pathfinder.core.modes

import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.FindUIEdge
import com.example.pathfinder.core.FindUIVertex
import com.example.pathfinder.core.UIGraph

class SetPriceMode(private val findUIVertex: FindUIVertex, private val findUIEdge: FindUIEdge) : TouchMode {
	override fun onTouch(event: MotionEvent, graph: UIGraph): Boolean {
		if (event.action != MotionEvent.ACTION_DOWN) return false
		val vertexId = findUIVertex.findIndex(PointF(event.x, event.y), graph)
		if (vertexId != -1) {
			graph.setVertexCost(vertexId, 10f)
			return true
		}
		val edge = findUIEdge.findIndex(PointF(event.x, event.y), graph)
		if (edge.from != -1) {
			graph.setEdgeCost(edge)
			return true
		}
		return false
	}
}