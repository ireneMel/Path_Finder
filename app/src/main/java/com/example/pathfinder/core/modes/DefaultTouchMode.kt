package com.example.pathfinder.core.modes

import android.view.MotionEvent
import com.example.pathfinder.core.UIGraph

object DefaultTouchMode : TouchMode {
	override fun onTouch(event: MotionEvent, graph: UIGraph) = false
}