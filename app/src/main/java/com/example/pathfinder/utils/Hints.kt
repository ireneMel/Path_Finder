package com.example.pathfinder.utils

import android.view.View
import com.codertainment.materialintro.MaterialIntroConfiguration
import com.codertainment.materialintro.sequence.SkipLocation
import com.codertainment.materialintro.shape.ShapeType

object Hints {
    fun MaterialIntroConfiguration.basicConfig(
        mTargetView: View,
        message: String
    ) {
        isDotViewEnabled = true
        isDotAnimationEnabled = true
        infoText = message
        infoTextAlignment = View.TEXT_ALIGNMENT_CENTER
        targetView = mTargetView
        showOnlyOnce = false
        shapeType = ShapeType.CIRCLE
        skipLocation = SkipLocation.TOP_RIGHT
        userClickAsDisplayed = true
        viewId = mTargetView.toString()
    }
}