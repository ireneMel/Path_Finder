package com.example.pathfinder.core.algorithms

import com.example.pathfinder.core.algorithms.Dijkstra.GraphState.*
import com.example.pathfinder.models.*
import java.util.*

interface GraphStep {
	val start: List<Int>
	val end: List<Int>
	val currentVertices: List<Int>
	val currentEdges: List<Edge>
	val usedVertices: Iterable<Int>
	val usedEdges: Iterable<Edge>
}

abstract class GraphAlgorithm(val start: List<Int>, val end: List<Int>, val graph: Graph) {
	protected data class GraphStepImpl(
		override val start: List<Int>,
		override val end: List<Int>,
		override val currentVertices: List<Int>,
		override val currentEdges: List<Edge>,
		override val usedVertices: HashSet<Int>,
		override val usedEdges: HashSet<Edge>
	) : GraphStep
	
	abstract fun generate(): Sequence<GraphStep>
}

class Dijkstra private constructor(start: List<Int>, end: List<Int>, graph: Graph) :
	GraphAlgorithm(start, end, graph) {
	private val _graph: Graph = if (graph is UnidirectionalGraph) graph.reversed() else graph
	
	constructor(start: Int, end: Int, graph: Graph) : this(listOf(start), listOf(end), graph)
	
	private val queue = PriorityQueue<EdgeTo>()
	private val dist: MutableList<Float> = MutableList(graph.vertices.size) {
		Float.POSITIVE_INFINITY
	}
	
	init {
		runAlgo()
	}
	
	private fun runAlgo() {
		end.forEach {
			queue.add(EdgeTo(it, 0f))
			dist[it] = 0f
		}
		
		var currentDistance: Float
		while (queue.isNotEmpty()) {
			val current = queue.poll() ?: break
			if (current.cost > dist[current.to]) continue
			for (edgeTo in _graph.edges[current.to]) {
				if (graph.vertices[edgeTo.to] == null) continue
				currentDistance =
					dist[current.to] + graph.vertices[edgeTo.to]!!.cost.nanToZero() + edgeTo.cost.nanToOne()
				if (currentDistance < dist[edgeTo.to]) {
					dist[edgeTo.to] = currentDistance
					queue.add(EdgeTo(edgeTo.to, dist[edgeTo.to]))
				}
			}
		}
	}
	
	private fun Float.nanToZero() = if (this.isNaN()) 0f else this
	private fun Float.nanToOne() = if (this.isNaN()) 1f else this
	
	private fun GraphStepImpl.next(): GraphStepImpl {
		val newVertices = hashSetOf<Int>()
		val newEdges = hashSetOf<Edge>()
		val minDist = currentVertices.minOf {
			_graph.edges[it].minOfOrNull { dist[it.to] } ?: Float.POSITIVE_INFINITY
		}
		for (from in currentVertices) {
			for (edge in _graph.edges[from]) {
				if (minDist == dist[edge.to]) {
					newVertices.add(edge.to)
					newEdges.add(Edge(from, edge.to, edge.cost))
				}
			}
		}
		return GraphStepImpl(
			start,
			end,
			newVertices.toList(),
			newEdges.toList(),
			newVertices.apply {clear(); addAll(usedVertices); addAll(currentVertices) },
			newEdges.apply {clear(); addAll(usedEdges); addAll(currentEdges) },
		)
	}
	
	private enum class GraphState {
		FIRST, MIDDLE, LAST, END
	}
	
	override fun generate(): Sequence<GraphStep> {
		if (start.all { dist[it] == Float.POSITIVE_INFINITY }) return emptySequence()
		var state = FIRST
		var currentState = GraphStepImpl(
			start, end, emptyList(), emptyList(), hashSetOf(), hashSetOf()
		)
		return generateSequence {
			if (state == END) return@generateSequence null
			val ret = currentState
			if (state == FIRST)
				currentState = GraphStepImpl(start, end, start, emptyList(), hashSetOf(), hashSetOf())
			if (state == MIDDLE)
				currentState = currentState.next()
			
			state = when (state) {
				FIRST  -> MIDDLE
				MIDDLE ->
					if (currentState.usedVertices.any { end.contains(it) }) LAST else MIDDLE
				LAST   -> END
				else   -> error("Unexpected state")
			}
			if (state == LAST)
				currentState = GraphStepImpl(start, end, emptyList(), emptyList(), currentState.usedVertices, currentState.usedEdges)
			ret
		}
	}
}