package com.example.pathfinder.core.modes

import android.graphics.Canvas
import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.FindUIVertex
import com.example.pathfinder.core.UIGraph
import com.example.pathfinder.core.UIVertex
import com.example.pathfinder.core.emptyPointF

class AddEdgeMode(private val findUIVertex: FindUIVertex) : TouchMode, DrawMode {
	private var start: UIVertex? = null
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
		start = findUIVertex.find(position, graph)
	}
	
	private fun drag(position: PointF) {
		end = position
	}
	
	private fun endDrag(position: PointF, graph: UIGraph) {
		val vertex = findUIVertex.find(position, graph)
		if (start != null && vertex != null) {
			graph.addEdge(start!!, vertex)
		}
		start = null
		end.x = Float.NaN
	}
	
	override fun onDraw(canvas: Canvas, graph: UIGraph) {
		if (start != null && end != emptyPointF) {
			canvas.drawLine(
				start!!.position.x, start!!.position.y, end.x, end.y, graph.edgeStrokePaint
			)
		}
		DefaultDrawMode.onDraw(canvas, graph)
	}
	
}