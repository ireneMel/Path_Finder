package com.example.pathfinder.core

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import androidx.core.graphics.withRotation
import com.example.pathfinder.core.uiGraph.*
import kotlin.math.atan2
import kotlin.math.sqrt

fun Canvas.drawVertex(vertex: UIVertex) {
	drawCircle(vertex.position.x, vertex.position.y, vertex.design.radius, vertex.design.paint)
	drawCircle(
		vertex.position.x,
		vertex.position.y,
		vertex.design.radius - (vertex.design.strokePaint.strokeWidth / 2f),
		vertex.design.strokePaint
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

private var degree: Float = 0f
private var bounds = Rect()
private var dist: Float = 0f
private var x: Float = 0f
private var y: Float = 0f
private var textStart = PointF()
private var textEnd = PointF()

fun Canvas.drawVertexText(label: UIVertexLabel) {
	label.textPaint.getTextBounds(label.text, 0, label.text.length, bounds)
	y = label.middle.y + (bounds.height() / 2f - bounds.bottom)
	drawText(label.text, label.middle.x, y, label.textPaint)
}

fun Canvas.drawEdgeText(label: UIEdgeLabel) {
	if (label.textStart.x > label.textEnd.x) {
		textStart.set(label.textEnd)
		textEnd.set(label.textStart)
	} else {
		textStart.set(label.textStart)
		textEnd.set(label.textEnd)
	}
	dist = sqrt(textStart.distanceSquaredTo(textEnd))
	degree = (atan2(textEnd.y - textStart.y, textEnd.x - textStart.x) * 180f / Math.PI).toFloat()
	x = textStart.x + dist / 2
	y = textStart.y - label.textPadding
	withRotation(degree, textStart.x, textStart.y) {
		label.textPaint.getTextBounds(label.text, 0, label.text.length, bounds)
		drawText(label.text, x, y, label.textPaint)
	}
}