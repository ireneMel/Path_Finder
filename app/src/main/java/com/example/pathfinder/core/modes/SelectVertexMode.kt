package com.example.pathfinder.core.modes

import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.uiGraph.EditUIGraph
import com.example.pathfinder.core.uiGraph.UIGraph
import com.example.pathfinder.core.uiGraph.finders.FindUIEdge
import com.example.pathfinder.core.uiGraph.finders.FindUIVertex
import com.example.pathfinder.models.Edge

class SelectVertexMode(
	private val findUIVertex: FindUIVertex,
	private val graph: UIGraph,
	private val onVertexSet: (vertexId: Int) -> Unit
) : TouchMode{
	override fun onTouch(event: MotionEvent): Boolean {
		if (event.action != MotionEvent.ACTION_DOWN) return false
		val vertexId = findUIVertex.findIndex(PointF(event.x, event.y), graph)
		if (vertexId != -1) {
			onVertexSet(vertexId)
			return true
		}
		return false
	}
}