package com.example.pathfinder.models

import android.graphics.PointF

data class Vertex(
	val position: PointF, var cost: Float = 0f
)

data class Edge(
	val to: Int, var cost: Float = 0f
)

class Graph(
	val vertices: MutableList<Vertex?> = mutableListOf(),
	val edges: MutableList<MutableList<Edge>> = mutableListOf()
)