package com.example.pathfinder.core.modes

import android.graphics.PointF
import android.view.MotionEvent
import com.example.pathfinder.core.UIGraph

object AddVertexMode : TouchMode {
	override fun onTouch(event: MotionEvent, graph: UIGraph): Boolean{
		if (event.action != MotionEvent.ACTION_DOWN) return false
		graph.addVertexWithLocalSize(PointF(event.x, event.y))
		return true
	}
}