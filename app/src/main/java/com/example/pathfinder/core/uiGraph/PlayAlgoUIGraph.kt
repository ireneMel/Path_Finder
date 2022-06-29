package com.example.pathfinder.core.uiGraph

import android.graphics.Paint
import com.example.pathfinder.core.algorithms.GraphStep
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph

class PlayAlgoUIGraph(
	vertexDesign: UIVertexDesign,
	edgeStrokePaint: Paint,
	textPaint: Paint,
	textPadding: Float,
	graph: Graph,
	width: Float = 1f,
	height: Float = 1f,
) : UIGraph(
	vertexDesign, edgeStrokePaint, textPaint, textPadding, graph, width, height
) {
	fun setGraphStep(graphStep: GraphStep, algoDesign: AlgoDesign) {
		graphStep.start.forEach {
			_vertices[it]?.design = algoDesign.startDesign
		}
		graphStep.end.forEach {
			_vertices[it]?.design = algoDesign.endDesign
		}
		graphStep.usedVertices.forEach {
			_vertices[it]?.design = algoDesign.usedDesign
		}
		graphStep.currentVertices.forEach {
			_vertices[it]?.design = algoDesign.currentDesign
		}
		graphStep.usedEdges.forEach { usedEdge ->
			_edges[usedEdge]?.strokePaint = algoDesign.usedEdgePaint
			_edges[Edge(usedEdge.to, usedEdge.from)]?.strokePaint = algoDesign.usedEdgePaint
		}
		graphStep.currentEdges.forEach { currentEdge ->
			_edges[currentEdge]?.strokePaint = algoDesign.currentEdgePaint
			_edges[Edge(currentEdge.to, currentEdge.from)]?.strokePaint =
				algoDesign.currentEdgePaint
		}
	}
	
	fun resetGraphPaint() {
		_vertices.values.forEach {
			it.design = vertexDesign
		}
		_edges.values.forEach {
			it.strokePaint = edgeStrokePaint
		}
	}
}