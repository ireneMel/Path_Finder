package com.example.pathfinder.core

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.pathfinder.core.modes.DefaultDrawMode
import com.example.pathfinder.core.modes.DefaultTouchMode
import com.example.pathfinder.core.modes.DrawMode
import com.example.pathfinder.core.modes.TouchMode
import com.example.pathfinder.core.uiGraph.UIGraph

class GraphView @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
	var graph: UIGraph? = null
		set(value) {
			field = value
			value?.resize(width.toFloat(), height.toFloat())
			invalidate()
		}
	
	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		graph?.resize(w.toFloat(), h.toFloat())
	}
	
	var drawMode: DrawMode? = null
	var touchMode: TouchMode = DefaultTouchMode
	
	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)
		if (canvas == null) return
		drawMode?.onDraw(canvas)
	}
	
	override fun onTouchEvent(event: MotionEvent): Boolean {
		val ret = touchMode.onTouch(event)
		return if (ret) {
			invalidate()
			true
		} else super.onTouchEvent(event)
	}
	
	fun updateGraph(){
		invalidate()
	}
	
}
