package com.example.pathfinder.core.modes

import android.graphics.Canvas
import android.view.MotionEvent
import com.example.pathfinder.core.uiGraph.UIGraph

class DefaultMode(graph: UIGraph) : CombinedMode {
	private val drawMode = DefaultDrawMode(graph)
	override fun onDraw(canvas: Canvas) {
		drawMode.onDraw(canvas)
	}
	
	override fun onTouch(event: MotionEvent): Boolean {
		return DefaultTouchMode.onTouch(event)
	}
}