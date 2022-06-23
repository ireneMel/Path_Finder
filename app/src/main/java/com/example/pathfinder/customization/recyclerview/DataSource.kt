package com.example.pathfinder.customization.recyclerview

import com.example.pathfinder.customization.ThemeNames
import com.example.pathfinder.models.ThemeModel

class DataSource {
    companion object {
        fun createDataSet(): List<ThemeModel> {
            return mutableListOf(
                ThemeModel(ThemeNames.BASIC),
                ThemeModel(ThemeNames.DRACULA),
                ThemeModel(ThemeNames.GRAPE),
                ThemeModel(ThemeNames.SEA),
                ThemeModel(ThemeNames.SPRING)
            )
        }
    }
}