package com.example.pathfinder.customization

import android.content.Context
import com.example.pathfinder.R

object ThemeManager {
	fun setCustomTheme(context: Context, theme: String) {
		when (theme) {
			ThemeNames.BASIC   -> context.setTheme(R.style.Theme_PathFinder)
			ThemeNames.SPRING  -> context.setTheme(R.style.Spring)
			ThemeNames.DRACULA -> context.setTheme(R.style.Dracula)
			ThemeNames.SEA     -> context.setTheme(R.style.Sea)
			ThemeNames.GRAPE   -> context.setTheme(R.style.Grape)
			else               -> context.setTheme(R.style.Theme_PathFinder)
		}
	}
}