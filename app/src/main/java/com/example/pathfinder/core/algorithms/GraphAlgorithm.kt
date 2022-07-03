package com.example.pathfinder.core.algorithms

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
	abstract fun generate(): List<GraphStep>
}

class Dijkstra private constructor(start: List<Int>, end: List<Int>, graph: Graph) :
	GraphAlgorithm(start, end, graph) {
	constructor(start: Int, end: Int, graph: Graph) : this(listOf(start), listOf(end), graph)
	
	private class VertexCost(val vertexId: Int, val cost: Float) : Comparable<VertexCost> {
		override fun compareTo(other: VertexCost) = cost.compareTo(other.cost)
	}
	
	private val queue = PriorityQueue<VertexCost>()
	private val dist: MutableMap<Int, Float> = mutableMapOf()
	private val cnt: MutableMap<Int, Int> = mutableMapOf()
	private val ans: MutableMap<Int, MutableList<Int>> = mutableMapOf()
	private fun addAns(from: Int, to: Int, clear: Boolean) {
		if (clear) ans[to]?.clear()
		if (ans[to] == null) ans[to] = mutableListOf()
		ans[to]?.add(from)
	}
	
	val pathCost: Float
	
	init {
		runAlgo()
		pathCost = start.minOf { dist[it]!! }
	}
	
	private fun runAlgo() {
		for ((id, _) in graph.vertices) dist[id] = Float.POSITIVE_INFINITY
		end.forEach {
			queue.add(VertexCost(it, 0f))
			dist[it] = 0f
		}
		
		var currentDistance: Float
		while (queue.isNotEmpty()) {
			val current = queue.poll() ?: break
			if (current.cost > dist[current.vertexId]!!) continue
			for ((to, cost) in graph.reversedEdges[current.vertexId] ?: continue) {
				currentDistance =
					dist[current.vertexId]!! + graph.vertices[to]!!.cost.nanToZero() + cost.nanToOne()
				if (currentDistance == dist[to]!!) {
					addAns(current.vertexId, to, false)
				}
				if (currentDistance < dist[to]!!) {
					addAns(current.vertexId, to, true)
					dist[to] = currentDistance
					queue.add(VertexCost(to, dist[to]!!))
				}
			}
		}
	}
	
	override fun generate(): List<GraphStep> {
		dist.forEach { (to, _) ->
			cnt[to] = 0
		}
		ans.forEach { (_, from) ->
			from.forEach {
				cnt[it] = cnt[it]!! + 1
			}
		}
		var step = GraphStepImpl(start, end, start, emptyList(), emptyList(), emptyList())
		val list = mutableListOf(step)
		while (step.currentVertices.isNotEmpty()) {
			val newVertices = mutableSetOf<Int>()
			val newEdges = mutableListOf<Edge>()
			val usedVertices = mutableListOf<Int>()
			val usedEdges = mutableListOf<Edge>()
			usedVertices.addAll(step.usedVertices)
			usedEdges.addAll(step.usedEdges)
			step.currentVertices.forEach { current ->
				if (cnt[current] == 0) {
					usedVertices.add(current)
					ans[current]?.forEach {
						cnt[it] = cnt[it]!! - 1
						newVertices.add(it)
						newEdges.add(Edge(current, it))
					}
				} else {
					newVertices.add(current)
				}
			}
			step.currentEdges.forEach {
				if (newVertices.contains(it.to)) newEdges.add(it)
				else usedEdges.add(it)
			}
			step = step.copy(
				currentVertices = newVertices.toList(),
				currentEdges = newEdges,
				usedVertices = usedVertices,
				usedEdges = usedEdges
			)
			list.add(step)
		}
		return list
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
}