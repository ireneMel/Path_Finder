package com.example.pathfinder.core

import android.graphics.Canvas

fun Canvas.drawVertex(vertex: UIVertex) {
	drawCircle(vertex.position.x, vertex.position.y, vertex.radius, vertex.paint)
	drawCircle(
		vertex.position.x,
		vertex.position.y,
		vertex.radius - (vertex.strokePaint.strokeWidth / 2f),
		vertex.strokePaint
	)
}

fun Canvas.drawEdge(edge: UIEdge) {
	drawLine(
		edge.startPosition.x,
		edge.startPosition.y,
		edge.endPosition.x,
		edge.endPosition.y,
		edge.strokePaint
	)
}