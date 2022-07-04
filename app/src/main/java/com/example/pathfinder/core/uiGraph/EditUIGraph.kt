package com.example.pathfinder.core.uiGraph

import android.graphics.PointF
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph
import com.example.pathfinder.models.Vertex

abstract class EditUIGraph(
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
	
	fun setVertexCost(index: Int, cost: Float) {
		if (cost.isNaN()) return
		_vertices[index]?.text = cost.toString()
		graph.vertices[index]?.cost = cost
	}
	
	abstract fun addEdge(from: Int, to: Int)
	
	fun removeEdge(edge: Edge) {
		graph.removeEdge(edge.from, edge.to)
		graph.removeEdge(edge.to, edge.from)
		_edges.remove(edge)
		_edges.remove(Edge(from = edge.to, to = edge.from))
	}
	
	fun setEdgeCost(edge: Edge) {
		if (edge.cost.isNaN()) return
		graph.setCost(edge)
		graph.setCost(edge.copy(from = edge.to, to = edge.from))
		_edges[edge]?.text = edge.cost.toString()
	}
}

class OneEditUIGraph(
	design: GraphDesign,
	graph: Graph,
	width: Float = 1f,
	height: Float = 1f,
) : EditUIGraph(
	design, graph, width, height
) {
	override fun addEdge(from: Int, to: Int) {
		val defaultCost = graph.edges[to]?.get(from) ?: Float.NaN
		
		_edges[Edge(from, to)] = UIEdge(
			from = from,
			to = to,
			strokePaint = design.edgeStrokePaint,
			text = if (defaultCost.isNaN()) "" else defaultCost.toString(),
			textPaint = design.textEdgePaint,
			textPadding = design.textPadding
		)
		graph.addEdge(from, to, defaultCost)
	}
}

class BiEditUIGraph(
	design: GraphDesign,
	graph: Graph,
	width: Float = 1f,
	height: Float = 1f,
) : EditUIGraph(
	design, graph, width, height
) {
	override fun addEdge(from: Int, to: Int) {
		_edges[Edge(from, to)] =
			UIEdge(from, to, design.edgeStrokePaint, "", design.textEdgePaint, design.textPadding)
		_edges[Edge(to, from)] =
			UIEdge(to, from, design.edgeStrokePaint, "", design.textEdgePaint, design.textPadding)
		graph.addEdge(from, to)
		graph.addEdge(to, from)
	}
}