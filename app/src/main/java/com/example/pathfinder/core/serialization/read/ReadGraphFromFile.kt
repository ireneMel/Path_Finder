package com.example.pathfinder.core.serialization.read

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.PointF
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import com.example.pathfinder.core.serialization.write.FILE_TYPE
import com.example.pathfinder.models.Graph
import com.example.pathfinder.models.Vertex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ReadState {
	object IDLE : ReadState()
	object READING : ReadState()
	object ERROR : ReadState()
	class FINISHED(val graph: Graph) : ReadState()
}

class ReadGraphFromFile(
	registry: ActivityResultRegistry,
	lifecycleOwner: LifecycleOwner,
	private val contentResolver: ContentResolver
) {
	
	companion object {
		private const val READ = "READ"
	}
	
	private val _state = MutableStateFlow<ReadState>(ReadState.IDLE)
	val state = _state.asStateFlow()
	fun openFile() {
		_state.value = ReadState.READING
		val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
			addCategory(Intent.CATEGORY_OPENABLE)
			type = FILE_TYPE
		}
		getResultOpenFile.launch(intent)
	}
	
	private val groupRegex = Regex("\\{[\\s\\S]*?\\}")
	private val itemRegex = Regex("\\([\\s\\S]*?\\)")
	
	private val getResultOpenFile = registry.register(
		READ,
		lifecycleOwner,
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			val data = result.data
			val uri = data?.data
			contentResolver.openInputStream(uri!!)?.use {
				val text = it.bufferedReader().readText()
				val results = groupRegex.findAll(text).take(2).toList()
				val vertices = readVertices(results[0].value)
				val edges = readEdges(results[1].value)
				_state.value = ReadState.FINISHED(Graph(vertices, edges))
				_state.value = ReadState.IDLE
			}
		} else {
			_state.value = ReadState.ERROR
			_state.value = ReadState.IDLE
		}
	}
	
	private fun readVertices(text: String): HashMap<Int, Vertex>{
		val map = hashMapOf<Int, Vertex>()
		itemRegex.findAll(text).forEach{
			map.readVertex(it.value.substring(1, it.value.length - 1))
		}
		return map
	}
	
	private fun HashMap<Int, Vertex>.readVertex(text: String) {
		val (id, x, y, cost) = text.split(' ')
		this[id.toInt()] = Vertex(PointF(x.toFloat(), y.toFloat()), cost.toFloat())
	}
	
	private fun readEdges(text: String): MutableMap<Int,MutableMap<Int, Float>> {
		val ret = mutableMapOf<Int, MutableMap<Int, Float>>()
		itemRegex.findAll(text).forEach {
			val (from, to, cost) = it.value.substring(1, it.value.length - 1).split(' ')
			val fromId = from.toInt()
			if (ret[fromId] == null) ret[fromId] = mutableMapOf()
			ret[fromId]!![to.toInt()] = cost.toFloat()
		}
		return ret
	}
	
}