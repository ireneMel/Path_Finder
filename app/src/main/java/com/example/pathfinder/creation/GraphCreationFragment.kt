package com.example.pathfinder.creation

import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.pathfinder.R
import com.example.pathfinder.core.algorithms.Dijkstra
import com.example.pathfinder.core.modes.*
import com.example.pathfinder.core.serialization.read.ReadGraphFromFile
import com.example.pathfinder.core.serialization.read.ReadState
import com.example.pathfinder.core.serialization.write.FileState
import com.example.pathfinder.core.serialization.write.SaveGraphToFile
import com.example.pathfinder.core.uiGraph.*
import com.example.pathfinder.core.uiGraph.finders.FindUIEdge
import com.example.pathfinder.core.uiGraph.finders.FindUIVertex
import com.example.pathfinder.databinding.FragmentGraphCreationBinding
import com.example.pathfinder.dialogs.GetPriceDialog
import com.example.pathfinder.models.Edge
import com.example.pathfinder.utils.getThemeColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("UNREACHABLE_CODE")
class GraphCreationFragment : Fragment(R.layout.fragment_graph_creation) {
	private lateinit var binding: FragmentGraphCreationBinding
	private lateinit var uiGraph: EditUIGraph
	
	private fun getPaint(color: Int, strokeWidth: Float? = null) = Paint().apply {
		this.color = color
		if (strokeWidth != null) this.strokeWidth = strokeWidth
	}
	
	private val primaryColor by lazy(LazyThreadSafetyMode.NONE) { getThemeColor(com.google.android.material.R.attr.colorPrimary) }
	private val primaryVariantColor by lazy(LazyThreadSafetyMode.NONE) { getThemeColor(com.google.android.material.R.attr.colorPrimaryVariant) }
	private val onPrimaryColor by lazy(LazyThreadSafetyMode.NONE) { getThemeColor(com.google.android.material.R.attr.colorOnPrimary) }
	private val secondaryColor by lazy(LazyThreadSafetyMode.NONE) { getThemeColor(com.google.android.material.R.attr.colorSecondary) }
	private val secondaryVariantColor by lazy(LazyThreadSafetyMode.NONE) {
		getThemeColor(com.google.android.material.R.attr.colorSecondaryVariant)
	}
	
	private val _1dp by lazy(LazyThreadSafetyMode.NONE) { resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT }
	private val defaultVertexDesign by lazy(LazyThreadSafetyMode.NONE) {
		UIVertexDesign(
			radius = 16 * _1dp,
			paint = getPaint(primaryColor),
			strokePaint = getPaint(primaryVariantColor, 3 * _1dp)
		)
	}
	
	private fun getDesign(): GraphDesign {
		return GraphDesign(
			defaultVertexDesign,
			defaultVertexDesign.strokePaint,
			Paint().apply { color = onPrimaryColor; textSize = 12 * _1dp },
			3 * _1dp
		)
	}
	
	private fun getAlgoDesign(): AlgoDesign {
		val usedColor = getPaint(getThemeColor(androidx.appcompat.R.attr.colorAccent), 3 * _1dp)
		
		val currentFillColor = getPaint(secondaryColor)
		
		val currentStrokeColor = getPaint(secondaryVariantColor, 3 * _1dp)
		
		return AlgoDesign(
			startDesign = defaultVertexDesign,
			endDesign = defaultVertexDesign,
			usedDesign = defaultVertexDesign.copy(strokePaint = usedColor),
			currentDesign = defaultVertexDesign.copy(
				paint = currentFillColor, strokePaint = currentStrokeColor
			),
			usedEdgePaint = usedColor,
			currentEdgePaint = currentStrokeColor
		)
	}
	
	private val viewModel: GraphCreationViewModel by viewModels {
		GraphCreationViewModel.Factory(getDesign())
	}
	
	companion object {
		var OPEN_CLICKED = false
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		
		binding = FragmentGraphCreationBinding.bind(view)
		val vertexFinder = FindUIVertex(60f)
		val edgeFinder = FindUIEdge(45f)
		var mode = 0
		val modes = listOf(
			AddVertexMode(uiGraph),
			AddEdgeMode(vertexFinder, uiGraph),
			RemoveMode(vertexFinder, edgeFinder, uiGraph),
			SetPriceMode(vertexFinder, edgeFinder, uiGraph, ::onVertexSet, ::onEdgeSet)
		)
		val texts = listOf("Add vertex", "Add edge", "Remove", "Set price")
		val drawMode = DefaultDrawMode(uiGraph)
		binding.graph.touchMode = modes[mode]
		binding.graph.graph = uiGraph
		binding.graph.drawMode = drawMode
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
		
		
		binding.visualize.setOnClickListener {
			lifecycleScope.launchWhenStarted {
				val graph = PlayAlgoUIGraph(
					design = getDesign(),
					graph = uiGraph.graph
				)
				binding.graph.graph = graph
				binding.graph.drawMode = DefaultDrawMode(graph)
				binding.graph.touchMode = DefaultTouchMode
				Dijkstra(
					graph.vertices.keys.first(), graph.vertices.keys.last(), graph.graph
				).generate().forEach {
					graph.setGraphStep(it, getAlgoDesign())
					Log.d("Debug141", "onViewCreated: $it")
					binding.graph.invalidate()
					delay(1000)
				}
				graph.resetGraphPaint()
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
			} else {
				binding.graph.drawMode = drawMode
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