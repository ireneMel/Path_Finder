package com.example.pathfinder.models

import android.graphics.PointF

data class Vertex(
	val position: PointF, var cost: Float = Float.NaN
)

data class Edge(
	val from: Int, val to: Int, var cost: Float = Float.NaN
){
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Edge) return false
		
		if (from != other.from) return false
		if (to != other.to) return false
		
		return true
	}
	
	override fun hashCode(): Int {
		var result = from
		result = 31 * result + to
		return result
	}
}

class Graph(
	val vertices: MutableMap<Int, Vertex> = mutableMapOf(),
	val edges: MutableMap<Int, MutableMap<Int, Float>> = mutableMapOf(),
	val reversedEdges: MutableMap<Int, MutableMap<Int, Float>> = mutableMapOf(),
){
	private var freeIndex: Int = (vertices.maxOfOrNull { it.key } ?: -1) + 1
	fun addVertex(vertex: Vertex): Int{
		vertices[freeIndex] = vertex
		return freeIndex++
	}
	fun addEdge(from: Int, to: Int, cost: Float = Float.NaN){
		if (edges[from] == null)edges[from] = mutableMapOf()
		if (reversedEdges[to] == null)reversedEdges[to] = mutableMapOf()
		edges[from]!![to] = cost
		reversedEdges[to]!![from] = cost
	}
}