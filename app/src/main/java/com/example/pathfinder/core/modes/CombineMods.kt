package com.example.pathfinder.core.modes

import android.graphics.Canvas
import android.view.MotionEvent

class CombineMods(private val drawMode: DrawMode, private val touchMode: TouchMode) : CombinedMode {
	override fun onDraw(canvas: Canvas) {
		drawMode.onDraw(canvas)
	}
	
	override fun onTouch(event: MotionEvent): Boolean {
		return touchMode.onTouch(event)
	}
}