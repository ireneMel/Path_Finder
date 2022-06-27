package com.example.pathfinder.core

import android.graphics.Paint
import android.graphics.PointF
import android.os.Build.VERSION_CODES.P
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph
import com.example.pathfinder.models.Vertex
import kotlin.math.sqrt

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

class UIGraph(
	val vertexRadius: Float,
	val vertexPaint: Paint,
	val vertexStrokePaint: Paint,
	val edgeStrokePaint: Paint,
	val textPaint: Paint,
	val textPadding: Float,
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
		override val paint: Paint,
		override val strokePaint: Paint,
		override var text: String,
		override val textPaint: Paint
	) : IUIVertex, UIVertexLabel {
		override val middle: PointF
			get() = position
	}
	
	private inner class UIEdge(
		val from: Int,
		val to: Int,
		override val strokePaint: Paint,
		override var text: String,
		override val textPaint: Paint,
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
	
	private val _vertices = mutableListOf<UIVertex?>()
	private val _edges = mutableListOf<UIEdge>()
	private var _graph = Graph()
	
	val vertices: List<IUIVertex?> get() = _vertices
	val edges: List<IUIEdge> get() = _edges
	val graphEdges: List<List<Edge>> get() = _graph.edges
	val graphVertices: List<Vertex?> get() = _graph.vertices
	
	fun resize(width: Float, height: Float) {
		val widthScale = width / this.width
		val heightScale = height / this.height
		if (widthScale <= 0f || heightScale <= 0f) return
		_vertices.forEach { vertex ->
			vertex?.position?.let {
				it.x *= widthScale
				it.y *= height
			}
		}
		this.width = width
		this.height = height
	}
	
	fun setGraph(graph: Graph) {
		_graph = graph
		_vertices.clear()
		_edges.clear()
		graphVertices.forEach {
			_vertices.add(
				if (it == null) null
				else UIVertex(
					position = PointF(it.position.x * width, it.position.y * height),
					radius = vertexRadius,
					paint = vertexPaint,
					strokePaint = vertexStrokePaint,
					text = if (it.cost == 0f) "" else it.cost.toString(),
					textPaint = textPaint
				)
			)
		}
		graphEdges.forEachIndexed { from, edges ->
			edges.forEach {
				_edges.add(
					UIEdge(
						from = from,
						to = it.to,
						strokePaint = vertexStrokePaint,
						text = if (it.cost == 0f) "" else it.cost.toString(),
						textPaint = textPaint,
						textPadding = textPadding
					)
				)
			}
		}
	}
	
	fun addVertex(position: PointF) {
		position.x *= width
		position.y *= height
		addVertexWithLocalSize(position)
	}
	
	fun addVertexWithLocalSize(position: PointF) {
		val vertex = UIVertex(position, vertexRadius, vertexPaint, vertexStrokePaint, "", textPaint)
		_vertices.add(vertex)
		_graph.edges.add(mutableListOf())
		_graph.vertices.add(Vertex(PointF(position.x / width, position.y / height)))
	}
	
	fun addEdge(from: Int, to: Int) {
		val edge = UIEdge(from, to, edgeStrokePaint, "", textPaint, textPadding)
		_edges.add(edge)
		_graph.edges[from].add(Edge(to))
	}
	
	fun addEdge(from: IUIVertex, to: IUIVertex) {
		val f = _vertices.indexOf(from as IUIVertex?)
		val t = _vertices.indexOf(to as IUIVertex?)
		if (f != -1 && t != -1) addEdge(f, t)
	}
	
	fun removeVertex(index: Int) {
		_vertices[index] = null
		_graph.vertices[index] = null
		_graph.edges[index].clear()
	}
	
	fun removeVertex(vertex: IUIVertex) {
		val id = _vertices.indexOf(vertex as IUIVertex?)
		if (id != -1) removeVertex(id)
	}
	
	fun removeEdge(index: Int) {
		val edge = _edges[index]
		_graph.edges[edge.from].removeAt(_graph.edges[edge.from].indexOfFirst { it.to == edge.to })
		_edges.removeAt(index)
	}
	
	fun removeEdge(edge: IUIEdge) {
		val id = _edges.indexOf(edge)
		if (id != -1) removeEdge(id)
	}
	
	fun setVertexCost(index: Int, cost: Float) {
		_vertices[index]?.text = cost.toString()
		_graph.vertices[index]?.cost = cost
	}
	
	fun setVertexCost(vertex: IUIVertex, cost: Float) {
		val id = _vertices.indexOf(vertex as IUIVertex?)
		if (id != -1) setVertexCost(id, cost)
	}
	
	fun setEdgeCost(index: Int, cost: Float) {
		val edge = _edges[index]
		edge.text = cost.toString()
		_graph.edges[edge.from].find { it.to == edge.to }?.cost = cost
	}
	
	fun setEdgeCost(edge: IUIEdge, cost: Float) {
		val id = _edges.indexOf(edge)
		if (id != -1) setEdgeCost(id, cost)
	}
}

class FindUIVertex(radius: Float) {
	private val radiusSquared = radius * radius
	
	fun find(position: PointF, uiGraph: UIGraph): IUIVertex? {
		return uiGraph.vertices.getOrNull(findIndex(position, uiGraph))
	}
	
	fun findIndex(position: PointF, uiGraph: UIGraph): Int {
		val vertices = uiGraph.vertices
		
		for (index in vertices.indices.reversed()) {
			if (vertices[index] == null) continue
			if (vertices[index]!!.position.distanceSquaredTo(position) < radiusSquared) return index
		}
		return -1
	}
}

class FindUIEdge(radius: Float) {
	private val radiusSquared = radius * radius
	
	fun find(position: PointF, uiGraph: UIGraph): IUIEdge? {
		return uiGraph.edges.getOrNull(findIndex(position, uiGraph))
	}
	
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
	
	fun findIndex(position: PointF, uiGraph: UIGraph): Int {
		val edges = uiGraph.edges
		
		for (index in edges.indices.reversed()) {
			if (edges[index].isEmpty()) continue
			if (position.distTo(edges[index]) < radiusSquared) return index
		}
		return -1
	}
}

val emptyPointF = PointF(Float.NaN, Float.NaN)

fun IUIEdge.isEmpty() = startPosition == emptyPointF || endPosition == emptyPointF

fun PointF.distanceSquaredTo(pointF: PointF): Float =
	(pointF.x - x) * (pointF.x - x) + (pointF.y - y) * (pointF.y - y)



