package com.example.pathfinder.models

data class Vertex(
	var cost: Float = 0f
)

data class Edge(
	val to: Int, var cost: Float = 0f
)

class Graph {
	val vertices = mutableListOf<Vertex?>()
	val edges: MutableList<MutableList<Edge>> = mutableListOf()
}