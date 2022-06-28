package com.example.pathfinder

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.example.pathfinder.core.*
import com.example.pathfinder.core.algorithms.Dijkstra
import com.example.pathfinder.core.modes.*
import com.example.pathfinder.core.serialization.read.ReadGraphFromFile
import com.example.pathfinder.core.serialization.read.ReadState
import com.example.pathfinder.core.serialization.write.FileState
import com.example.pathfinder.core.serialization.write.SaveGraphToFile
import com.example.pathfinder.databinding.FragmentGraphCreationBinding
import com.example.pathfinder.dialogs.GetPriceDialog
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph
import com.example.pathfinder.utils.getThemeColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("UNREACHABLE_CODE")
class GraphCreationFragment : Fragment(R.layout.fragment_graph_creation) {
	private lateinit var binding: FragmentGraphCreationBinding
	private lateinit var uiGraph: UIGraph
	
	companion object {
		var OPEN_CLICKED = false
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		val primaryColor = getThemeColor(com.google.android.material.R.attr.colorPrimary)
		val primaryVariantColor =
			getThemeColor(com.google.android.material.R.attr.colorPrimaryVariant)
		val onPrimaryColor = getThemeColor(com.google.android.material.R.attr.colorOnPrimary)
		val secondaryColor = getThemeColor(com.google.android.material.R.attr.colorSecondary)
		val secondaryVariantColor =
			getThemeColor(com.google.android.material.R.attr.colorSecondaryVariant)
		
		fun getPaint(color: Int, strokeWidth: Float? = null) = Paint().apply {
			this.color = color
			if (strokeWidth != null) this.strokeWidth = strokeWidth
		}
		
		val _1dp = resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
		val fillColor = getPaint(primaryColor)
		val strokeColor = getPaint(primaryVariantColor, 3 * _1dp)
		binding = FragmentGraphCreationBinding.bind(view)
		uiGraph = UIGraph(
			vertexRadius = 16 * _1dp,
			vertexPaint = fillColor,
			vertexStrokePaint = strokeColor,
			edgeStrokePaint = strokeColor,
			textPaint = Paint().apply { color = onPrimaryColor; textSize = 12 * _1dp },
			textPadding = 3 * _1dp,
			graph = Graph()
		)
		val vertexFinder = FindUIVertex(60f)
		val edgeFinder = FindUIEdge(45f)
		var mode = 0
		val modes = listOf(
			AddVertexMode,
			AddEdgeMode(vertexFinder),
			RemoveMode(vertexFinder, edgeFinder),
			SetPriceMode(vertexFinder, edgeFinder, ::onVertexSet, ::onEdgeSet)
		)
		val texts = listOf("Add vertex", "Add edge", "Remove", "Set price")
		binding.graph.touchMode = modes[mode]
		binding.graph.graph = uiGraph
		binding.buttonChange.text = texts[mode]
		
		lifecycleScope.launchWhenStarted {
			graphReader.state.collect {
				when (it) {
					ReadState.ERROR       -> makeToast("Error while reading file.")
					is ReadState.FINISHED -> {
						uiGraph.graph = it.graph
						makeToast("Success")
					}
					else                  -> {}
				}
			}
		}
		
		val usedColor = getPaint(getThemeColor(androidx.appcompat.R.attr.colorAccent), 3*_1dp)
		
		val currentFillColor = getPaint(secondaryColor)
		
		val currentStrokeColor = getPaint(secondaryVariantColor, 3*_1dp)
		
		val algoPaint = AlgoPaint(
			startPaint = fillColor,
			startStrokePaint = strokeColor,
			endPaint = fillColor,
			endStrokePaint = strokeColor,
			usedPaint = fillColor,
			usedStrokePaint = usedColor,
			currentPaint = currentFillColor,
			currentStrokePaint = currentStrokeColor,
			usedEdgePaint = usedColor,
			currentEdgePaint = currentStrokeColor
		)
		
		binding.visualize.setOnClickListener {
			lifecycleScope.launchWhenStarted {
				Dijkstra(
					uiGraph.vertices.keys.first(), uiGraph.vertices.keys.last(), uiGraph.graph
				).generate().forEach {
					uiGraph.setGraphStep(it, algoPaint)
					Log.d("Debug141", "onViewCreated: $it")
					binding.graph.invalidate()
					delay(1000)
				}
				uiGraph.resetGraphPaint()
				binding.graph.invalidate()
			}
		}
		
		//load graph when corresponding button was clicked
		if (OPEN_CLICKED) {
			graphReader.openFile()
			OPEN_CLICKED = false
		}
		
		binding.open.setOnClickListener { graphReader.openFile() }
		
		binding.buttonChange.setOnClickListener {
			mode = (mode + 1) % modes.size
			binding.graph.touchMode = modes[mode]
			binding.buttonChange.text = texts[mode]
			if (modes[mode] is DrawMode) {
				binding.graph.drawMode = modes[mode] as DrawMode
			}
		}
		
		lifecycleScope.launchWhenStarted {
			graphSaver.state.collect {
				when (it) {
					FileState.ERROR  -> makeToast("Error while saving file.")
					FileState.CLOSED -> makeToast("Success")
					else             -> {}
				}
			}
		}
		binding.save.setOnClickListener {
			val graph = binding.graph.graph
			if (graph != null) {
				graphSaver.createFile(graph)
			} else {
				makeToast("The canvas must not be empty")
			}
		}
	}
	
	private fun onVertexSet(id: Int) {
		lifecycleScope.launchWhenStarted {
			uiGraph.setVertexCost(id, getPrice())
			binding.graph.invalidate()
		}
	}
	
	private fun onEdgeSet(edge: Edge) {
		lifecycleScope.launchWhenStarted {
			edge.cost = getPrice()
			uiGraph.setEdgeCost(edge)
			binding.graph.invalidate()
		}
	}
	
	private val dialog = GetPriceDialog()
	private suspend fun getPrice(): Float = suspendCoroutine {
		dialog.show(childFragmentManager, null)
		dialog.setFragmentResultListener(GetPriceDialog.RESULT) { _, bundle ->
			val result = bundle.getString(GetPriceDialog.RESULT)
			it.resume(
				try {
					result!!.toFloat()
				} catch (_: Exception) {
					Float.NaN
				}
			)
		}
	}
	
	private val graphSaver: SaveGraphToFile by lazy {
		SaveGraphToFile(
			requireActivity().activityResultRegistry,
			viewLifecycleOwner,
			requireActivity().contentResolver
		)
	}
	
	private val graphReader: ReadGraphFromFile by lazy {
		ReadGraphFromFile(
			requireActivity().activityResultRegistry,
			viewLifecycleOwner,
			requireActivity().contentResolver
		)
	}
	
	private fun makeToast(text: String) =
		Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
	
}