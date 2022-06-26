package com.example.pathfinder.core.modes

import android.graphics.Canvas
import com.example.pathfinder.core.UIGraph
import com.example.pathfinder.core.drawEdge
import com.example.pathfinder.core.drawVertex
import com.example.pathfinder.core.emptyPointF

object DefaultDrawMode : DrawMode {
	override fun onDraw(canvas: Canvas, graph: UIGraph) {
		graph.edges.forEach {
			if (it.startPosition != emptyPointF && it.endPosition != emptyPointF) canvas.drawEdge(it)
		}
		graph.vertices.forEach {
			if (it != null) canvas.drawVertex(it)
		}
	}
}