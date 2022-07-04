package com.example.pathfinder.core.uiGraph

import android.graphics.PointF
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph
import com.example.pathfinder.models.Vertex

class EditUIGraph(
	design: GraphDesign,
	graph: Graph,
	width: Float = 1f,
	height: Float = 1f,
) : UIGraph(
	design, graph, width, height
) {
	fun addVertex(position: PointF) {
		position.x *= width
		position.y *= height
		addVertexWithLocalSize(position)
	}
	
	fun addVertexWithLocalSize(position: PointF) {
		val index = graph.addVertex(Vertex(PointF(position.x / width, position.y / height)))
		_vertices[index] = UIVertex(position, design.vertexDesign, "", design.textVertexPaint)
	}
	
	fun addEdge(from: Int, to: Int) {
		_edges[Edge(from, to)] =
			UIEdge(from, to, design.edgeStrokePaint, "", design.textEdgePaint, design.textPadding)
		graph.addEdge(from, to)
	}
	
	fun removeVertex(index: Int) {
		_vertices.remove(index)
		graph.vertices.remove(index)
		
		for ((from, _) in graph.reversedEdges[index] ?: return) {
			graph.edges[from]?.remove(index)
			_edges.remove(Edge(from, index))
		}
		for ((to, _) in graph.edges[index] ?: return) {
			graph.reversedEdges[to]?.remove(index)
			_edges.remove(Edge(index, to))
		}
		
		graph.edges.remove(index)
		graph.reversedEdges.remove(index)
	}
	
	fun removeEdge(edge: Edge) {
		graph.edges[edge.from]?.remove(edge.to)
		_edges.remove(edge)
	}
	
	fun setVertexCost(index: Int, cost: Float) {
		if (cost.isNaN()) return
		_vertices[index]?.text = cost.toString()
		graph.vertices[index]?.cost = cost
	}
	
	fun setEdgeCost(edge: Edge) {
		if (edge.cost.isNaN()) return
		graph.edges[edge.from]!![edge.to] = edge.cost
		_edges[edge]?.text = edge.cost.toString()
	}
}