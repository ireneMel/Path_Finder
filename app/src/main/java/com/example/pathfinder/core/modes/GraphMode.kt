package com.example.pathfinder.core.modes

import android.graphics.Canvas
import android.view.MotionEvent
import com.example.pathfinder.core.uiGraph.UIGraph

interface DrawMode {
	fun onDraw(canvas: Canvas)
}

interface TouchMode {
	fun onTouch(event: MotionEvent): Boolean
}

interface CombinedMode : DrawMode, TouchMode