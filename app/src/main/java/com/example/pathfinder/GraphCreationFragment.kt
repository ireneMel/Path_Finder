package com.example.pathfinder

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.pathfinder.core.FindUIEdge
import com.example.pathfinder.core.FindUIVertex
import com.example.pathfinder.core.UIGraph
import com.example.pathfinder.core.UIVertex
import com.example.pathfinder.core.modes.*
import com.example.pathfinder.databinding.FragmentGraphCreationBinding
import java.io.BufferedReader
import java.io.InputStreamReader

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
        binding.open.setOnClickListener {
            openFile()
        }

        binding.save.setOnClickListener {
            createFile()
        }
    }

    companion object {
        private const val FILE_EXTENSION = "txt"
    }

    private fun createFile() {
        if (binding.graph.graph?.vertices?.get(0) == null) {
            Toast.makeText(requireContext(), "The canvas must not be empty", Toast.LENGTH_SHORT)
                .show()
        } else {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/$FILE_EXTENSION"
                putExtra(Intent.EXTRA_TITLE, "path.$FILE_EXTENSION")
            }
            getResultCreateFile.launch(intent)
        }
    }

    private val getResultCreateFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val uri = data?.data

                val vertexes = binding.graph.graph?.vertices
                val edges = binding.graph.graph?.edges
                val outputStream = requireContext().contentResolver.openOutputStream(uri!!)

                //vertices
                outputStream?.write("{ ".toByteArray())
                for (item in vertexes!!) {
                    outputStream?.write(
                        savePatternVertex(
                            item?.position?.x ?: 0f,
                            item?.position?.y ?: 0f
                        ).toByteArray()
                    )
                }
                outputStream?.write(" } ".toByteArray())

                //edges
                outputStream?.write("{".toByteArray())
                for (item in edges!!) {
                    outputStream?.write(
                        savePatternEdge(
                            item.startPosition,
                            item.endPosition
                        ).toByteArray()
                    )
                }
                outputStream?.write(" }".toByteArray())

                outputStream?.close()
                Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/$FILE_EXTENSION"
        }
        getResultOpenFile.launch(intent)
    }

    private val getResultOpenFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val uri = data?.data

                val reader =
                    BufferedReader(
                        InputStreamReader(
                            requireContext().contentResolver.openInputStream(
                                uri!!
                            )
                        )
                    )

                val inputGraphText = reader.use { it.readText() }.split("\\s")
                reader.close()

                var restoredVertices: List<UIVertex?> = mutableListOf()
                val restoredEdges: List<UIVertex?> = mutableListOf()
//                val graph:UIGraph = UIGraph()
                //read vertices
                val i = 0
                for (item in inputGraphText) {
                    if (item.equals("{")) continue
                    if (item.equals("}")) break
                }
            }
        }

    private fun savePatternVertex(x: Float = 0f, y: Float = 0f, cost: Float = 0f): String =
        "($x) ($y) ($cost)\n"

    private fun savePatternEdge(
        start: PointF = PointF(0f, 0f),
        end: PointF = PointF(0f, 0f),
        cost: Float = 0f
    ): String =
        " (${start.x}) (${start.y}) " +
                "(${end.x}) (${end.y}) " +
                "($cost)\n"
}