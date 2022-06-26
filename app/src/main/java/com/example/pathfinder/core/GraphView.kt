package com.example.pathfinder.core

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.pathfinder.core.modes.DefaultDrawMode
import com.example.pathfinder.core.modes.DefaultTouchMode
import com.example.pathfinder.core.modes.DrawMode
import com.example.pathfinder.core.modes.TouchMode

class GraphView @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
	var graph: UIGraph? = null
		set(value) {
			field = value
			value?.resize(width.toFloat(), height.toFloat())
			invalidate()
		}
	
	init {
		graph = UIGraph(24f, Paint().apply { color = Color.GRAY }, Paint(), Paint())
		graph?.addVertex(PointF(.5f, .5f))
	}
	
	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		graph?.resize(w.toFloat(), h.toFloat())
	}
	
	var drawMode: DrawMode = DefaultDrawMode
	var touchMode: TouchMode = DefaultTouchMode
	
	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)
		if (canvas == null) return
		graph?.let {
			drawMode.onDraw(canvas, it)
		}
	}
	
	override fun onTouchEvent(event: MotionEvent): Boolean {
		val ret = graph?.let { touchMode.onTouch(event, it) }
		return if (ret == true) {
			invalidate()
			true
		} else super.onTouchEvent(event)
	}
	
}
