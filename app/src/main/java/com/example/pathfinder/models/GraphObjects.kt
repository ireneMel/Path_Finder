package com.example.pathfinder.models

import android.graphics.PointF

data class Vertex(
	val position: PointF, var cost: Float = Float.NaN
)

data class EdgeTo(
	val to: Int, var cost: Float = Float.NaN
) : Comparable<EdgeTo> {
	override operator fun compareTo(other: EdgeTo): Int {
		return cost.compareTo(other.cost)
	}
}

data class Edge(
	val from: Int, val to: Int, var cost: Float = Float.NaN
)

abstract class Graph(
	val vertices: MutableList<Vertex?> = mutableListOf(),
	val edges: MutableList<MutableList<EdgeTo>> = mutableListOf()
) {
	abstract fun addEdge(from: Int, to: Int, cost: Float = Float.NaN)
	abstract fun setCost(from: Int, to: Int, cost: Float)
}

class UnidirectionalGraph(
	vertices: MutableList<Vertex?> = mutableListOf(),
	edges: MutableList<MutableList<EdgeTo>> = mutableListOf()
) : Graph(vertices, edges) {
	override fun addEdge(from: Int, to: Int, cost: Float) {
		edges[from].add(EdgeTo(to, cost))
	}
	
	override fun setCost(from: Int, to: Int, cost: Float) {
		edges[from].find { it.to == to }?.cost = cost
	}
}

class BidirectionalGraph(
	vertices: MutableList<Vertex?> = mutableListOf(),
	edges: MutableList<MutableList<EdgeTo>> = mutableListOf()
) : Graph(vertices, edges) {
	override fun addEdge(from: Int, to: Int, cost: Float) {
		edges[from].add(EdgeTo(to, cost))
		edges[to].add(EdgeTo(from, cost))
	}
	
	override fun setCost(from: Int, to: Int, cost: Float) {
		edges[from].find { it.to == to }?.cost = cost
		edges[to].find { it.to == from }?.cost = cost
	}
}

fun UnidirectionalGraph.reversed(): UnidirectionalGraph {
	val edges = MutableList(edges.size) { mutableListOf<EdgeTo>() }
	for (from in this.edges.indices) {
		for (edgeTo in this.edges[from]) {
			edges[edgeTo.to].add(EdgeTo(from, edgeTo.cost))
		}
	}
	return UnidirectionalGraph(vertices, edges)
}