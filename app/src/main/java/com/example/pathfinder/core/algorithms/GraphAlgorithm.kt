package com.example.pathfinder.core.algorithms

import com.example.pathfinder.core.algorithms.Dijkstra.GraphState.*
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph
import java.util.*

interface GraphStep {
	val start: List<Int>
	val end: List<Int>
	val currentVertices: List<Int>
	val currentEdges: List<Edge>
	val usedVertices: List<Int>
	val usedEdges: List<Edge>
}

abstract class GraphAlgorithm(val start: List<Int>, val end: List<Int>, val graph: Graph) {
	abstract fun generate(): Sequence<GraphStep>
}

class Dijkstra private constructor(start: List<Int>, end: List<Int>, graph: Graph) :
	GraphAlgorithm(start, end, graph) {
	constructor(start: Int, end: Int, graph: Graph) : this(listOf(start), listOf(end), graph)
	
	private class VertexCost(val vertexId: Int, val cost: Float) : Comparable<VertexCost> {
		override fun compareTo(other: VertexCost) = cost.compareTo(other.cost)
	}
	
	private val queue = PriorityQueue<VertexCost>()
	private val dist: MutableMap<Int, Float> = mutableMapOf()
	
	init {
		runAlgo()
	}
	
	private fun runAlgo() {
		for ((id, _) in graph.vertices)dist[id] = Float.POSITIVE_INFINITY
		end.forEach {
			queue.add(VertexCost(it, 0f))
			dist[it] = 0f
		}
		
		var currentDistance: Float
		while (queue.isNotEmpty()) {
			val current = queue.poll() ?: break
			if (current.cost > dist[current.vertexId]!!) continue
			for ((to, cost) in graph.reversedEdges[current.vertexId]?:continue) {
				currentDistance =
					dist[current.vertexId]!! + graph.vertices[to]!!.cost.nanToZero() + cost.nanToOne()
				if (currentDistance < dist[to]!!) {
					dist[to] = currentDistance
					queue.add(VertexCost(to, dist[to]!!))
				}
			}
		}
	}
	
	private fun Float.nanToZero() = if (this.isNaN()) 0f else this
	private fun Float.nanToOne() = if (this.isNaN()) 1f else this
	
	private data class GraphStepImpl(
		override val start: List<Int>,
		override val end: List<Int>,
		override val currentVertices: List<Int>,
		override val currentEdges: List<Edge>,
		override val usedVertices: List<Int>,
		override val usedEdges: List<Edge>
	) : GraphStep
	
	private val usedVertices = hashSetOf<Int>()
	private val usedEdges = hashSetOf<Edge>()
	private val newVertices = hashSetOf<Int>()
	private val newEdges = hashSetOf<Edge>()
	
	private fun GraphStepImpl.next(): GraphStepImpl {
		newVertices.clear()
		newEdges.clear()
		
		val minDist = currentVertices.minOf {
			graph.edges[it]?.minOfOrNull { dist[it.key]!! } ?: Float.POSITIVE_INFINITY
		}
		
		for (from in currentVertices) {
			for ((to, cost) in graph.edges[from]?:continue) {
				if (minDist == dist[to]) {
					newVertices.add(to)
					newEdges.add(Edge(from, to, cost))
				}
			}
		}
		
		this@Dijkstra.usedVertices.addAll(currentVertices)
		this@Dijkstra.usedEdges.addAll(currentEdges)
		
		return GraphStepImpl(
			start,
			end,
			newVertices.toList(),
			newEdges.toList(),
			this@Dijkstra.usedVertices.toList(),
			this@Dijkstra.usedEdges.toList()
		)
	}
	
	private enum class GraphState {
		FIRST, MIDDLE, LAST, END
	}
	
	override fun generate(): Sequence<GraphStep> {
		if (start.all { dist[it] == Float.POSITIVE_INFINITY }) return emptySequence()
		var state = FIRST
		var currentState =
			GraphStepImpl(start, end, emptyList(), emptyList(), emptyList(), emptyList())
		return generateSequence {
			if (state == END) return@generateSequence null
			val ret = currentState
			if (state == FIRST) currentState =
				GraphStepImpl(start, end, start, emptyList(), emptyList(), emptyList())
			if (state == MIDDLE) currentState = currentState.next()
			
			state = when (state) {
				FIRST -> MIDDLE
				MIDDLE -> if (currentState.usedVertices.any { end.contains(it) }) LAST else MIDDLE
				LAST -> END
				else -> error("Unexpected state")
			}
			if (state == LAST) currentState = GraphStepImpl(
				start,
				end,
				emptyList(),
				emptyList(),
				currentState.usedVertices,
				currentState.usedEdges
			)
			ret
		}
	}
}