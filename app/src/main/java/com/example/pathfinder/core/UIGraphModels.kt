package com.example.pathfinder.core

import android.graphics.Paint
import android.graphics.PointF
import android.text.TextPaint
import androidx.core.graphics.plus
import com.example.pathfinder.models.Edge
import com.example.pathfinder.models.Graph
import com.example.pathfinder.models.Vertex
import kotlin.math.min

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
	val textPaint: TextPaint
}

private typealias IUIVertex = UIVertex
private typealias IUIEdge = UIEdge
private typealias IUILabel = UILabel

class UIGraph(
	val vertexRadius: Float,
	val vertexPaint: Paint,
	val vertexStrokePaint: Paint,
	val edgeStrokePaint: Paint,
	private var width: Float = 1f,
	private var height: Float = 1f,
) {
	init {
		vertexPaint.style = Paint.Style.FILL
		vertexStrokePaint.style = Paint.Style.STROKE
		edgeStrokePaint.style = Paint.Style.STROKE
	}
	
	private data class UIVertex(
		override val position: PointF,
		override val radius: Float,
		override val paint: Paint,
		override val strokePaint: Paint
	) : IUIVertex
	
	private inner class UIEdge(
		val from: Int, val to: Int, override val strokePaint: Paint
	) : IUIEdge {
		override val startPosition: PointF
			get() = _vertices[from]?.position ?: emptyPointF
		override val endPosition: PointF
			get() = _vertices[to]?.position ?: emptyPointF
	}
	
	private val _vertices = mutableListOf<UIVertex?>()
	private val _edges = mutableListOf<UIEdge>()
	private val _graph = Graph()
	
	val vertices: List<IUIVertex?> get() = _vertices
	val edges: List<IUIEdge> get() = _edges
	
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
	
	fun addVertex(position: PointF) {
		position.x *= width
		position.y *= height
		addVertexWithLocalSize(position)
	}
	
	fun addVertexWithLocalSize(position: PointF) {
		val vertex = UIVertex(position, vertexRadius, vertexPaint, vertexStrokePaint)
		_vertices.add(vertex)
		_graph.edges.add(mutableListOf())
		_graph.vertices.add(Vertex())
	}
	
	fun addEdge(from: Int, to: Int) {
		val edge = UIEdge(from, to, edgeStrokePaint)
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
}

class FindUIVertex(private val radius: Float = Float.POSITIVE_INFINITY) {
	
	fun find(position: PointF, uiGraph: UIGraph): IUIVertex? {
		val vertices = uiGraph.vertices
		
		var minIndex = -1
		var dist: Float
		var minDist = 0f
		
		for (index in vertices.indices) {
			if (vertices[index] == null) continue
			
			dist = vertices[index]!!.position.distanceSquaredTo(position)
			
			if (dist > radius * radius) continue
			
			if (minIndex == -1 || minDist > dist) {
				minIndex = index
				minDist = dist
			}
		}
		
		return vertices.getOrNull(minIndex)
	}
}

val emptyPointF = PointF(Float.NaN, Float.NaN)

fun PointF.distanceSquaredTo(pointF: PointF): Float {
	return (pointF.x - x) * (pointF.x - x) + (pointF.y - y) * (pointF.y - y)
}


