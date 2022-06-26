package com.example.pathfinder.core.modes

import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.FindUIVertex
import com.example.pathfinder.core.UIGraph

class RemoveVertexMode(private val findUIVertex: FindUIVertex) : TouchMode {
	override fun onTouch(event: MotionEvent, graph: UIGraph): Boolean {
		if (event.action != MotionEvent.ACTION_DOWN) return false
		val vertex = findUIVertex.find(PointF(event.x, event.y), graph)
		if (vertex != null) graph.removeVertex(vertex)
		return true
	}
}