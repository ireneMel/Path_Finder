package com.example.pathfinder.core

import android.graphics.Paint
import android.graphics.PointF
import com.example.pathfinder.core.algorithms.GraphStep
import com.example.pathfinder.models.*

interface UIVertex {
	val position: PointF
	val radius: Float
	val paint: Paint
	val strokePaint: Paint
}

interface UIEdge {
	val startPosition: PointF
	val endPosition: PointF
	val strokePaint: Paint
}

interface UILabel {
	val text: String
	val textPaint: Paint
}

interface UIVertexLabel : UILabel {
	val middle: PointF
}

interface UIEdgeLabel : UILabel {
	val textStart: PointF
	val textEnd: PointF
	val textPadding: Float
}

private typealias IUIVertex = UIVertex
private typealias IUIEdge = UIEdge
private typealias IUILabel = UILabel

data class AlgoPaint(
	val startPaint: Paint,
	val startStrokePaint: Paint,
	val endPaint: Paint,
	val endStrokePaint: Paint,
	val usedPaint: Paint,
	val usedStrokePaint: Paint,
	val currentPaint: Paint,
	val currentStrokePaint: Paint,
	val usedEdgePaint: Paint,
	val currentEdgePaint: Paint,
) {
	init {
		startPaint.style = Paint.Style.FILL
		endPaint.style = Paint.Style.FILL
		usedPaint.style = Paint.Style.FILL
		currentPaint.style = Paint.Style.FILL
		startStrokePaint.style = Paint.Style.STROKE
		endStrokePaint.style = Paint.Style.STROKE
		usedStrokePaint.style = Paint.Style.STROKE
		currentStrokePaint.style = Paint.Style.STROKE
		usedEdgePaint.style = Paint.Style.STROKE
		currentEdgePaint.style = Paint.Style.STROKE
	}
}

class UIGraph(
	val vertexRadius: Float,
	val vertexPaint: Paint,
	val vertexStrokePaint: Paint,
	val edgeStrokePaint: Paint,
	val textPaint: Paint,
	val textPadding: Float,
	graph: Graph,
	private var width: Float = 1f,
	private var height: Float = 1f,
) {
	init {
		vertexPaint.style = Paint.Style.FILL
		vertexStrokePaint.style = Paint.Style.STROKE
		edgeStrokePaint.style = Paint.Style.STROKE
		textPaint.isAntiAlias = true
		textPaint.textAlign = Paint.Align.CENTER
	}
	
	private data class UIVertex(
		override val position: PointF,
		override val radius: Float,
		override var paint: Paint,
		override var strokePaint: Paint,
		override var text: String,
		override var textPaint: Paint
	) : IUIVertex, UIVertexLabel {
		override val middle: PointF
			get() = position
	}
	
	private inner class UIEdge(
		val from: Int,
		val to: Int,
		override var strokePaint: Paint,
		override var text: String,
		override var textPaint: Paint,
		override val textPadding: Float
	) : IUIEdge, UIEdgeLabel {
		override val startPosition: PointF
			get() = _vertices[from]?.position ?: emptyPointF
		override val endPosition: PointF
			get() = _vertices[to]?.position ?: emptyPointF
		override val textStart: PointF
			get() = startPosition
		override val textEnd: PointF
			get() = endPosition
	}
	
	private val _vertices = mutableMapOf<Int, UIVertex>()
	private val _edges = mutableMapOf<Edge, UIEdge>()
	
	val vertices: Map<Int, IUIVertex> = _vertices
	val edges: Map<Edge, IUIEdge> = _edges
	
	var graph: Graph = graph
		set(value) {
			field = value
			_vertices.clear()
			_edges.clear()
			
			for ((id, vertex) in graph.vertices) {
				_vertices[id] = UIVertex(
					position = PointF(vertex.position.x * width, vertex.position.y * height),
					radius = vertexRadius,
					paint = vertexPaint,
					strokePaint = vertexStrokePaint,
					text = if (vertex.cost.isNaN()) "" else vertex.cost.toString(),
					textPaint = textPaint
				)
			}
			for ((from, map) in graph.edges) {
				for ((to, cost) in map) {
					_edges[Edge(from, to, cost)] = UIEdge(
						from = from,
						to = to,
						strokePaint = vertexStrokePaint,
						text = if (cost.isNaN()) "" else cost.toString(),
						textPaint = textPaint,
						textPadding = textPadding
					)
				}
			}
		}
	
	fun resize(width: Float, height: Float) {
		val widthScale = width / this.width
		val heightScale = height / this.height
		if (widthScale <= 0f || heightScale <= 0f) return
		for ((_, vertex) in _vertices) {
			vertex.position.x *= widthScale
			vertex.position.y *= heightScale
		}
		this.width = width
		this.height = height
	}
	
	fun addVertex(position: PointF) {
		position.x *= width
		position.y *= height
		addVertexWithLocalSize(position)
	}
	
	fun addVertexWithLocalSize(position: PointF) {
		val index = graph.addVertex(Vertex(PointF(position.x / width, position.y / height)))
		_vertices[index] =
			UIVertex(position, vertexRadius, vertexPaint, vertexStrokePaint, "", textPaint)
	}
	
	fun addEdge(from: Int, to: Int) { _edges[Edge(from, to)] = UIEdge(from, to, edgeStrokePaint, "", textPaint, textPadding)
		graph.addEdge(from, to)
	}
	
	fun removeVertex(index: Int) {
		_vertices.remove(index)
		graph.vertices.remove(index)
		
		for ((from, _) in graph.reversedEdges[index]!!) {
			graph.edges[from]?.remove(index)
			_edges.remove(Edge(from, index))
		}
		for ((to, _) in graph.edges[index]!!) {
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
	
	fun setGraphStep(graphStep: GraphStep, algoPaint: AlgoPaint) {
		graphStep.start.forEach {
			_vertices[it]?.paint = algoPaint.startPaint
			_vertices[it]?.strokePaint = algoPaint.startStrokePaint
		}
		graphStep.end.forEach {
			_vertices[it]?.paint = algoPaint.endPaint
			_vertices[it]?.strokePaint = algoPaint.endStrokePaint
		}
		graphStep.usedVertices.forEach {
			_vertices[it]?.paint = algoPaint.usedPaint
			_vertices[it]?.strokePaint = algoPaint.usedStrokePaint
		}
		graphStep.currentVertices.forEach {
			_vertices[it]?.paint = algoPaint.currentPaint
			_vertices[it]?.strokePaint = algoPaint.currentStrokePaint
		}
		graphStep.usedEdges.forEach { usedEdge ->
			_edges[usedEdge]?.strokePaint = algoPaint.usedEdgePaint
			_edges[Edge(usedEdge.to, usedEdge.from)]?.strokePaint = algoPaint.usedEdgePaint
		}
		graphStep.currentEdges.forEach { currentEdge ->
			_edges[currentEdge]?.strokePaint = algoPaint.currentEdgePaint
			_edges[Edge(currentEdge.to, currentEdge.from)]?.strokePaint = algoPaint.currentEdgePaint
		}
	}
	
	fun resetGraphPaint() {
		_vertices.values.forEach {
			it.paint = vertexPaint
			it.strokePaint = vertexStrokePaint
		}
		_edges.values.forEach {
			it.strokePaint = edgeStrokePaint
		}
	}
}

class FindUIVertex(radius: Float) {
	private val radiusSquared = radius * radius
	
	fun findIndex(position: PointF, uiGraph: UIGraph): Int {
		val vertices = uiGraph.vertices
		for ((index, vertex) in vertices) {
			if (vertex.position.distanceSquaredTo(position) < radiusSquared) return index
		}
		return -1
	}
}

class FindUIEdge(radius: Float) {
	private val radiusSquared = radius * radius
	
	private fun PointF.distTo(edge: IUIEdge) = shortestDistance(
		edge.startPosition.x, edge.startPosition.y, edge.endPosition.x, edge.endPosition.y, x, y
	)
	
	private fun shortestDistance(
		x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float
	): Float {
		val px = x2 - x1
		val py = y2 - y1
		val temp = px * px + py * py
		var u = ((x3 - x1) * px + (y3 - y1) * py) / temp
		if (u > 1) {
			u = 1f
		} else if (u < 0) {
			u = 0f
		}
		val x = x1 + u * px
		val y = y1 + u * py
		val dx = x - x3
		val dy = y - y3
		return dx * dx + dy * dy
	}
	
	fun findIndex(position: PointF, uiGraph: UIGraph): Edge {
		val edges = uiGraph.edges
		for ((edge, uiEdge) in edges) {
			if (position.distTo(uiEdge) < radiusSquared) return edge
		}
		return Edge(-1, -1)
	}
}

val emptyPointF = PointF(Float.NaN, Float.NaN)

fun IUIEdge.isEmpty() = startPosition == emptyPointF || endPosition == emptyPointF

fun PointF.distanceSquaredTo(pointF: PointF): Float =
	(pointF.x - x) * (pointF.x - x) + (pointF.y - y) * (pointF.y - y)



