package com.example.pathfinder.core.modes

import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.uiGraph.EditUIGraph
import com.example.pathfinder.core.uiGraph.UIGraph
import com.example.pathfinder.models.Graph

class AddVertexMode(private val graph: EditUIGraph) : TouchMode {
	override fun onTouch(event: MotionEvent): Boolean{
		if (event.action != MotionEvent.ACTION_DOWN) return false
		graph.addVertexWithLocalSize(PointF(event.x, event.y))
		return true
	}
}