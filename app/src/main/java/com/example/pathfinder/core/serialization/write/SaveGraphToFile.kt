package com.example.pathfinder.core.serialization.write

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import com.example.pathfinder.core.UIGraph
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Vertex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.properties.Delegates

class SaveGraphToFile(
	registry: ActivityResultRegistry,
	lifecycleOwner: LifecycleOwner,
	private val contentResolver: ContentResolver
) {
	companion object {
		private const val WRITE = "WRITE"
	}
	
	private var uiGraph: UIGraph by Delegates.notNull()
	private val _state = MutableStateFlow(FileState.OPENED)
	val state = _state.asStateFlow()
	fun createFile(graph: UIGraph) {
		this.uiGraph = graph
		_state.value = FileState.OPENED
		if (graph.vertices.isEmpty()) {
			_state.value = FileState.ERROR
		} else {
			val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
				addCategory(Intent.CATEGORY_OPENABLE)
				type = FILE_TYPE
				putExtra(Intent.EXTRA_TITLE, "path")
			}
			getResultCreateFile.launch(intent)
		}
	}
	
	private val getResultCreateFile = registry.register(
		WRITE, lifecycleOwner, ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			val data = result.data
			val uri = data?.data
			
			contentResolver.openOutputStream(uri!!, "rwt")?.use {
				it.write("{\n".toByteArray())
				val graph = uiGraph.graph
				for ((id, vertex) in graph.vertices) {
					it.write(vertex.save(id).toByteArray())
				}
				it.write("}\n".toByteArray())
				
				//edges
				it.write("{\n".toByteArray())
				for ((from, map) in graph.edges){
					for ((to, cost) in map){
						it.write(saveEdge(from, to, cost).toByteArray())
					}
				}
				it.write("}".toByteArray())
			}
			_state.value = FileState.CLOSED
		} else {
			_state.value = FileState.ERROR
		}
	}
	
	private fun Vertex.save(id: Int): String =
		"($id ${position.x} ${position.y} $cost)\n"
	
	private fun saveEdge(from: Int, to: Int, cost: Float): String = "($from $to $cost)\n"
	
}