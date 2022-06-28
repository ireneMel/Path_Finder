package com.example.pathfinder.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.fragment.app.Fragment

fun Context.getThemeColor(@AttrRes color: Int) = TypedValue().run {
	theme.resolveAttribute(color, this, true)
	data
}

fun Fragment.getThemeColor(@AttrRes color: Int) = requireContext().getThemeColor(color)