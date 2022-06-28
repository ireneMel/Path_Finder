package com.example.pathfinder

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pathfinder.core.AlgoPaint
import com.example.pathfinder.core.FindUIEdge
import com.example.pathfinder.core.FindUIVertex
import com.example.pathfinder.core.UIGraph
import com.example.pathfinder.core.algorithms.Dijkstra
import com.example.pathfinder.core.algorithms.GraphAlgorithm
import com.example.pathfinder.core.modes.*
import com.example.pathfinder.core.serialization.read.ReadGraphFromFile
import com.example.pathfinder.core.serialization.read.ReadState
import com.example.pathfinder.core.serialization.write.FileState
import com.example.pathfinder.core.serialization.write.SaveGraphToFile
import com.example.pathfinder.databinding.FragmentGraphCreationBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

@Suppress("UNREACHABLE_CODE")
class GraphCreationFragment : Fragment(R.layout.fragment_graph_creation) {
    private lateinit var binding: FragmentGraphCreationBinding
    private lateinit var uiGraph: UIGraph

    companion object {
        var OPEN_CLICKED = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGraphCreationBinding.bind(view)
		val fillColor = Paint().apply {
			color = Color.CYAN
		}
		uiGraph = UIGraph(25f, fillColor, Paint().apply {
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

        lifecycleScope.launchWhenStarted {
            graphReader.state.collect {
                when (it) {
                    ReadState.ERROR -> makeToast("Error while reading file.")
                    is ReadState.FINISHED -> {
                        uiGraph.setGraph(it.graph)
                        makeToast("Success")
                    }
                    else -> {}
                }
            }
        }
	
		val startColor = Paint().apply {
			color = Color.BLUE
			strokeWidth = 3f
		}
	
		val currentColor = Paint().apply {
			color = Color.RED
			strokeWidth = 3f
		}
	
		val algoPaint = AlgoPaint(
			startPaint = fillColor,
			startStrokePaint = uiGraph.vertexStrokePaint,
			endPaint = fillColor,
			endStrokePaint = uiGraph.vertexStrokePaint,
			usedPaint = fillColor,
			usedStrokePaint = startColor,
			currentPaint = fillColor,
			currentStrokePaint = currentColor,
			usedEdgePaint = startColor,
			currentEdgePaint = currentColor
		)
	
		binding.visualize.setOnClickListener {
			lifecycleScope.launchWhenStarted {
				Dijkstra(0, uiGraph.vertices.lastIndex, uiGraph._graph).generate().forEach {
					uiGraph.setGraphStep(it, algoPaint)
					Log.d("Debug141", "onViewCreated: $it")
					binding.graph.invalidate()
					delay(1000)
				}
				uiGraph.clearGraph()
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
                    FileState.ERROR -> makeToast("Error while saving file.")
                    FileState.CLOSED -> makeToast("Success")
                    else -> {}
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