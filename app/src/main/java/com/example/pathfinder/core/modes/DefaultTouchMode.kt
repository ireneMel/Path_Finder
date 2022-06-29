package com.example.pathfinder.core.modes

import android.view.MotionEvent
import com.example.pathfinder.core.uiGraph.UIGraph

object DefaultTouchMode : TouchMode {
	override fun onTouch(event: MotionEvent) = false
}