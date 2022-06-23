package com.example.pathfinder.customization

import android.content.Context
import android.content.SharedPreferences

class ThemeStorage {

    companion object {
        private const val sharedPreferencesName = "theme_data"
        private const val key = "theme"

        fun saveThemeColor(context: Context, themeName: String?) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(key, themeName)
            editor.apply()
        }

        fun getThemeColor(context: Context): String? {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
            return sharedPreferences.getString(key, "Basic")
        }
    }
}