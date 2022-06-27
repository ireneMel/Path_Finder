package com.example.pathfinder

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pathfinder.core.FindUIEdge
import com.example.pathfinder.core.FindUIVertex
import com.example.pathfinder.core.UIGraph
import com.example.pathfinder.core.modes.*
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
		}, Paint().apply {
			color = Color.GRAY
			textSize = 18f
		}, 4f
		)
		val vertexFinder = FindUIVertex(60f)
		val edgeFinder = FindUIEdge(45f)
		var mode = 0
		val modes = listOf(
			AddVertexMode,
			AddEdgeMode(vertexFinder),
			RemoveVertexMode(vertexFinder, edgeFinder),
			SetPriceMode(vertexFinder, edgeFinder)
		)
		val texts = listOf("Add vertex", "Add edge", "Remove", "Set price")
		binding.graph.touchMode = modes[mode]
		binding.graph.graph = uiGraph
		binding.buttonChange.text = texts[mode]
		
		binding.buttonChange.setOnClickListener {
			mode = (mode + 1) % modes.size
			binding.graph.touchMode = modes[mode]
			binding.buttonChange.text = texts[mode]
			if (modes[mode] is DrawMode) {
				binding.graph.drawMode = modes[mode] as DrawMode
			}
		}
	}
}