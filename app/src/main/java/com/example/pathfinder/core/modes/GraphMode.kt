package com.example.pathfinder.core.modes

import android.graphics.Canvas
import android.view.MotionEvent
import com.example.pathfinder.core.UIGraph

interface DrawMode{
	fun onDraw(canvas: Canvas, graph: UIGraph)
}

interface TouchMode{
	fun onTouch(event: MotionEvent, graph: UIGraph): Boolean
}