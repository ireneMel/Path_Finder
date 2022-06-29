package com.example.pathfinder.core.uiGraph

import android.graphics.Paint
import android.graphics.PointF
import com.example.pathfinder.core.algorithms.GraphStep
import com.example.pathfinder.models.*

data class UIVertexDesign(
	val radius: Float, val paint: Paint, val strokePaint: Paint
) {
	init {
		paint.style = Paint.Style.FILL
		strokePaint.style = Paint.Style.STROKE
	}
}

interface UIVertex {
	val position: PointF
	val design: UIVertexDesign
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

data class AlgoDesign(
	val startDesign: UIVertexDesign,
	val endDesign: UIVertexDesign,
	val usedDesign: UIVertexDesign,
	val currentDesign: UIVertexDesign,
	val usedEdgePaint: Paint,
	val currentEdgePaint: Paint,
) {
	init {
		usedEdgePaint.style = Paint.Style.STROKE
		currentEdgePaint.style = Paint.Style.STROKE
	}
}

class EditUIGraph(
	vertexDesign: UIVertexDesign,
	edgeStrokePaint: Paint,
	textPaint: Paint,
	textPadding: Float,
	graph: Graph,
	width: Float = 1f,
	height: Float = 1f,
) : UIGraph(
	vertexDesign, edgeStrokePaint, textPaint, textPadding, graph, width, height
) {
	fun addVertex(position: PointF) {
		position.x *= width
		position.y *= height
		addVertexWithLocalSize(position)
	}
	
	fun addVertexWithLocalSize(position: PointF) {
		val index = graph.addVertex(Vertex(PointF(position.x / width, position.y / height)))
		_vertices[index] = UIVertex(position, vertexDesign, "", textPaint)
	}
	
	fun addEdge(from: Int, to: Int) {
		_edges[Edge(from, to)] = UIEdge(from, to, edgeStrokePaint, "", textPaint, textPadding)
		graph.addEdge(from, to)
	}
	
	fun removeVertex(index: Int) {
		_vertices.remove(index)
		graph.vertices.remove(index)
		
		for ((from, _) in graph.reversedEdges[index]?:return) {
			graph.edges[from]?.remove(index)
			_edges.remove(Edge(from, index))
		}
		for ((to, _) in graph.edges[index]?:return) {
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

class PlayAlgoUIGraph(
	vertexDesign: UIVertexDesign,
	edgeStrokePaint: Paint,
	textPaint: Paint,
	textPadding: Float,
	graph: Graph,
	width: Float = 1f,
	height: Float = 1f,
) : UIGraph(
	vertexDesign, edgeStrokePaint, textPaint, textPadding, graph, width, height
) {
	fun setGraphStep(graphStep: GraphStep, algoDesign: AlgoDesign) {
		graphStep.start.forEach {
			_vertices[it]?.design = algoDesign.startDesign
		}
		graphStep.end.forEach {
			_vertices[it]?.design = algoDesign.endDesign
		}
		graphStep.usedVertices.forEach {
			_vertices[it]?.design = algoDesign.usedDesign
		}
		graphStep.currentVertices.forEach {
			_vertices[it]?.design = algoDesign.currentDesign
		}
		graphStep.usedEdges.forEach { usedEdge ->
			_edges[usedEdge]?.strokePaint = algoDesign.usedEdgePaint
			_edges[Edge(usedEdge.to, usedEdge.from)]?.strokePaint = algoDesign.usedEdgePaint
		}
		graphStep.currentEdges.forEach { currentEdge ->
			_edges[currentEdge]?.strokePaint = algoDesign.currentEdgePaint
			_edges[Edge(currentEdge.to, currentEdge.from)]?.strokePaint =
				algoDesign.currentEdgePaint
		}
	}
	
	fun resetGraphPaint() {
		_vertices.values.forEach {
			it.design = vertexDesign
		}
		_edges.values.forEach {
			it.strokePaint = edgeStrokePaint
		}
	}
}

abstract class UIGraph(
	val vertexDesign: UIVertexDesign,
	val edgeStrokePaint: Paint,
	val textPaint: Paint,
	val textPadding: Float,
	graph: Graph,
	protected var width: Float = 1f,
	protected var height: Float = 1f,
) {
	init {
		edgeStrokePaint.style = Paint.Style.STROKE
		textPaint.isAntiAlias = true
		textPaint.textAlign = Paint.Align.CENTER
	}
	
	protected data class UIVertex(
		override val position: PointF,
		override var design: UIVertexDesign,
		override var text: String,
		override var textPaint: Paint
	) : IUIVertex, UIVertexLabel {
		override val middle: PointF
			get() = position
	}
	
	protected inner class UIEdge(
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
	
	protected val _vertices = mutableMapOf<Int, UIVertex>()
	protected val _edges = mutableMapOf<Edge, UIEdge>()
	
	val vertices: Map<Int, IUIVertex> = _vertices
	val edges: Map<Edge, IUIEdge> = _edges
	
	init {
		installGraph(graph)
	}
	
	private fun Vertex.toUI(id: Int): UIVertex {
		return UIVertex(
			position = PointF(position.x * width, position.y * height),
			design = vertexDesign,
			text = if (cost.isNaN()) "" else cost.toString(),
			textPaint = textPaint
		)
	}
	
	private fun createUIEdge(from: Int, to: Int, cost: Float): UIEdge {
		return UIEdge(
			from = from,
			to = to,
			strokePaint = edgeStrokePaint,
			text = if (cost.isNaN()) "" else cost.toString(),
			textPaint = textPaint,
			textPadding = textPadding
		)
	}
	
	var graph: Graph = graph
		set(value) {
			field = value
			installGraph(value)
		}
	
	private fun installGraph(graph: Graph){
		_vertices.clear()
		_edges.clear()
		
		for ((id, vertex) in graph.vertices) {
			_vertices[id] = vertex.toUI(id)
		}
		for ((from, map) in graph.edges) {
			for ((to, cost) in map) {
				_edges[Edge(from, to, cost)] = createUIEdge(from, to, cost)
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



