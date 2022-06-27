package com.example.pathfinder.core.modes

import android.graphics.Canvas
import com.example.pathfinder.core.*

object DefaultDrawMode : DrawMode {
	override fun onDraw(canvas: Canvas, graph: UIGraph) {
		graph.edges.forEach {
			if (!it.isEmpty()){
				canvas.drawEdge(it)
				if (it is UIEdgeLabel && it.text.isNotBlank()) canvas.drawEdgeText(it)
			}
		}
		graph.vertices.forEach {
			if (it != null) {
				canvas.drawVertex(it)
				if (it is UIVertexLabel && it.text.isNotBlank()) canvas.drawVertexText(it)
			}
		}
	}
}