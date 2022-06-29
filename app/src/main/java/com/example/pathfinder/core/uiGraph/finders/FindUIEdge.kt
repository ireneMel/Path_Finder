package com.example.pathfinder.core.uiGraph.finders

import android.graphics.PointF
import com.example.pathfinder.core.uiGraph.UIEdge
import com.example.pathfinder.core.uiGraph.UIGraph
import com.example.pathfinder.models.Edge

class FindUIEdge(radius: Float) {
	private val radiusSquared = radius * radius
	
	private fun PointF.distTo(edge: UIEdge) = shortestDistance(
		edge.startPosition.x, edge.startPosition.y, edge.endPosition.x, edge.endPosition.y, x, y
	)
	
	private fun shortestDistance(
		x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float
	): Float {
		val px = x2 - x1
		val py = y2 - y1
		val temp = px * px + py * py
		var u = ((x3 - x1) * px + (y3 - y1) * py) / temp
		if (u > 1) {
			u = 1f
		} else if (u < 0) {
			u = 0f
		}
		val x = x1 + u * px
		val y = y1 + u * py
		val dx = x - x3
		val dy = y - y3
		return dx * dx + dy * dy
	}
	
	fun findIndex(position: PointF, uiGraph: UIGraph): Edge {
		val edges = uiGraph.edges
		for ((edge, uiEdge) in edges) {
			if (position.distTo(uiEdge) < radiusSquared) return edge
		}
		return Edge(-1, -1)
	}
}