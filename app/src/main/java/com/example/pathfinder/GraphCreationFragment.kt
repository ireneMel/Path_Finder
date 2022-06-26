package com.example.pathfinder

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pathfinder.core.FindUIVertex
import com.example.pathfinder.core.UIGraph
import com.example.pathfinder.core.modes.AddEdgeMode
import com.example.pathfinder.core.modes.AddVertexMode
import com.example.pathfinder.core.modes.DrawMode
import com.example.pathfinder.core.modes.RemoveVertexMode
import com.example.pathfinder.databinding.FragmentGraphCreationBinding

class GraphCreationFragment : Fragment(R.layout.fragment_graph_creation) {
	private lateinit var binding: FragmentGraphCreationBinding
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentGraphCreationBinding.bind(view)
		val uiGraph = UIGraph(25f, Paint().apply {
			color = Color.CYAN
		}, Paint().apply {
			color = Color.GRAY
			strokeWidth = 3f
		}, Paint().apply {
			color = Color.GRAY
			strokeWidth = 3f
		})
		val finder = FindUIVertex(60f)
		var mode = 0
		val modes = listOf(AddVertexMode, AddEdgeMode(finder), RemoveVertexMode(finder))
		val texts = listOf("Add vertex", "Add edge", "Remove vertex")
		binding.graph.touchMode = modes[mode]
		binding.graph.graph = uiGraph
		binding.buttonChange.text = texts[mode]
		
		binding.buttonChange.setOnClickListener {
			mode = (mode + 1) % modes.size
			binding.graph.touchMode = modes[mode]
			binding.buttonChange.text = texts[mode]
			if (modes[mode] is DrawMode){
				binding.graph.drawMode = modes[mode] as DrawMode
			}
		}
	}
}