package com.example.pathfinder.core.modes

import android.graphics.Canvas
import com.example.pathfinder.core.*

object DefaultDrawMode : DrawMode {
	override fun onDraw(canvas: Canvas, graph: UIGraph) {
		for ((_, edge) in graph.edges) {
			canvas.drawEdge(edge)
			if (edge is UIEdgeLabel && edge.text.isNotBlank()) canvas.drawEdgeText(edge)
		}
		for ((_, vertex) in graph.vertices) {
			canvas.drawVertex(vertex)
			if (vertex is UIVertexLabel && vertex.text.isNotBlank()) canvas.drawVertexText(vertex)
		}
	}
}