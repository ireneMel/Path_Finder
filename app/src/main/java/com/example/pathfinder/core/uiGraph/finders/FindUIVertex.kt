package com.example.pathfinder.core.uiGraph.finders

import android.graphics.PointF
import com.example.pathfinder.core.uiGraph.UIGraph
import com.example.pathfinder.core.uiGraph.distanceSquaredTo

class FindUIVertex(radius: Float) {
	private val radiusSquared = radius * radius
	
	fun findIndex(position: PointF, uiGraph: UIGraph): Int {
		val vertices = uiGraph.vertices
		for ((index, vertex) in vertices) {
			if (vertex.position.distanceSquaredTo(position) < radiusSquared) return index
		}
		return -1
	}
}