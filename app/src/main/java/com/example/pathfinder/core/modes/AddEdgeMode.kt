package com.example.pathfinder.core.modes

import android.graphics.Canvas
import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.FindUIVertex
import com.example.pathfinder.core.UIGraph
import com.example.pathfinder.core.UIVertex
import com.example.pathfinder.core.emptyPointF

class AddEdgeMode(private val findUIVertex: FindUIVertex) : TouchMode, DrawMode {
	private var startIndex: Int = -1
	private val startPosition = PointF()
	private var end = emptyPointF
	
	override fun onTouch(event: MotionEvent, graph: UIGraph): Boolean {
		when (event.action) {
			MotionEvent.ACTION_DOWN                          -> startDrag(
				PointF(event.x, event.y), graph
			)
			MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> endDrag(
				PointF(event.x, event.y), graph
			)
			MotionEvent.ACTION_MOVE                          -> drag(
				PointF(event.x, event.y)
			)
		}
		return true
	}
	
	private fun startDrag(position: PointF, graph: UIGraph) {
		startIndex = findUIVertex.findIndex(position, graph)
		graph.vertices[startIndex]?.let { startPosition.set(it.position) }
	}
	
	private fun drag(position: PointF) {
		end = position
	}
	
	private fun endDrag(position: PointF, graph: UIGraph) {
		val endIndex = findUIVertex.findIndex(position, graph)
		if (startIndex != -1 && endIndex != -1) {
			graph.addEdge(startIndex, endIndex)
		}
		startIndex = -1
		end.x = Float.NaN
	}
	
	
	override fun onDraw(canvas: Canvas, graph: UIGraph) {
		if (startIndex != -1 && end != emptyPointF) {
			canvas.drawLine(
				startPosition.x, startPosition.y, end.x, end.y, graph.edgeStrokePaint
			)
		}
		DefaultDrawMode.onDraw(canvas, graph)
	}
	
}