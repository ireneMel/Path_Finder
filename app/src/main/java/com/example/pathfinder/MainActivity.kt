package com.example.pathfinder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.commit
import com.example.pathfinder.customization.ThemeManager
import com.example.pathfinder.customization.ThemeStorage

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		ThemeManager.setCustomTheme(
			this,
			ThemeStorage.getThemeColor(this).toString()
		)
		if (savedInstanceState == null) {
			supportFragmentManager.commit {
				replace(R.id.container, GraphCreationFragment())
			}
		}
	}
}