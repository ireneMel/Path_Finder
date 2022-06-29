package com.example.pathfinder.core.modes

import android.graphics.Canvas
import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.uiGraph.EditUIGraph
import com.example.pathfinder.core.uiGraph.FindUIVertex
import com.example.pathfinder.core.uiGraph.UIGraph
import com.example.pathfinder.core.uiGraph.emptyPointF

class AddEdgeMode(private val findUIVertex: FindUIVertex, private val graph: EditUIGraph) : TouchMode, DrawMode {
	private var startIndex: Int = -1
	private val startPosition = PointF()
	private var end = emptyPointF
	private val defaultDrawMode = DefaultDrawMode(graph)
	
	override fun onTouch(event: MotionEvent): Boolean {
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
	
	private fun startDrag(position: PointF, graph: EditUIGraph) {
		startIndex = findUIVertex.findIndex(position, graph)
		graph.vertices[startIndex]?.let { startPosition.set(it.position) }
	}
	
	private fun drag(position: PointF) {
		end = position
	}
	
	private fun endDrag(position: PointF, graph: EditUIGraph) {
		val endIndex = findUIVertex.findIndex(position, graph)
		if (startIndex != -1 && endIndex != -1) {
			graph.addEdge(startIndex, endIndex)
		}
		startIndex = -1
		end.x = Float.NaN
	}
	
	
	override fun onDraw(canvas: Canvas) {
		if (startIndex != -1 && end != emptyPointF) {
			canvas.drawLine(
				startPosition.x, startPosition.y, end.x, end.y, graph.edgeStrokePaint
			)
		}
		defaultDrawMode.onDraw(canvas)
	}
	
}