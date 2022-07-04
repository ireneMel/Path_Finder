package com.example.pathfinder.core.uiGraph

import android.graphics.Paint
import android.graphics.PointF
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph
import com.example.pathfinder.models.Vertex

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

data class GraphDesign(
	val vertexDesign: UIVertexDesign,
	val edgeStrokePaint: Paint,
	val textEdgePaint: Paint,
	val textVertexPaint: Paint,
	val textPadding: Float,
){
	init {
		edgeStrokePaint.style = Paint.Style.STROKE
		textVertexPaint.isAntiAlias = true
		textVertexPaint.textAlign = Paint.Align.CENTER

		textEdgePaint.isAntiAlias = true
		textEdgePaint.textAlign = Paint.Align.CENTER
	}
}

abstract class UIGraph(
	val design: GraphDesign,
	graph: Graph,
	protected var width: Float = 1f,
	protected var height: Float = 1f,
) {
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
			design = design.vertexDesign,
			text = if (cost.isNaN()) "" else cost.toString(),
			textPaint = design.textVertexPaint
		)
	}
	
	private fun createUIEdge(from: Int, to: Int, cost: Float): UIEdge {
		return UIEdge(
			from = from,
			to = to,
			strokePaint = design.edgeStrokePaint,
			text = if (cost.isNaN()) "" else cost.toString(),
			textPaint = design.textEdgePaint,
			textPadding = design.textPadding
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
		if (width == this.width && height == this.height) return
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

val emptyPointF = PointF(Float.NaN, Float.NaN)

fun PointF.distanceSquaredTo(pointF: PointF): Float =
	(pointF.x - x) * (pointF.x - x) + (pointF.y - y) * (pointF.y - y)



