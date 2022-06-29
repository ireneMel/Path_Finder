package com.example.pathfinder.fragments.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pathfinder.core.uiGraph.AlgoDesign
import com.example.pathfinder.core.uiGraph.GraphDesign
import com.example.pathfinder.models.Graph

interface GraphProvider {
	val graph: Graph
	val design: GraphDesign
	val algoDesign: AlgoDesign
}

class GraphViewModel(override val design: GraphDesign, override val algoDesign: AlgoDesign) :
	ViewModel(), GraphProvider {
	override val graph = Graph()
	class Factory(
		private val design: GraphDesign,
		private val algoDesign: AlgoDesign,
	) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			return GraphViewModel(design, algoDesign) as T
		}
	}
}