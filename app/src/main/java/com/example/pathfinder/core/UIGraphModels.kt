package com.example.pathfinder.core

import android.graphics.Paint
import android.graphics.PointF
import com.example.pathfinder.core.algorithms.GraphStep
import com.example.pathfinder.models.BidirectionalGraph
import com.example.pathfinder.models.EdgeTo
import com.example.pathfinder.models.Graph
import com.example.pathfinder.models.Vertex

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
){
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
	
	private val _vertices = mutableListOf<UIVertex?>()
	private val _edges = mutableListOf<MutableList<UIEdge>>()
	var _graph: Graph = BidirectionalGraph()
		private set
	
	val vertices: List<IUIVertex?> get() = _vertices
	val edges: List<List<IUIEdge>> get() = _edges
	val graphEdges: List<List<EdgeTo>> get() = _graph.edges
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
					text = if (it.cost.isNaN()) "" else it.cost.toString(),
					textPaint = textPaint
				)
			)
		}
		graphEdges.forEachIndexed { from, edges ->
			_edges.add(edges.mapTo(mutableListOf()) {
				UIEdge(
					from = from,
					to = it.to,
					strokePaint = vertexStrokePaint,
					text = if (it.cost.isNaN()) "" else it.cost.toString(),
					textPaint = textPaint,
					textPadding = textPadding
				)
			})
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
		_edges.add(mutableListOf())
		_graph.edges.add(mutableListOf())
		_graph.vertices.add(Vertex(PointF(position.x / width, position.y / height)))
	}
	
	fun addEdge(from: Int, to: Int) {
		_edges[from].add(UIEdge(from, to, edgeStrokePaint, "", textPaint, textPadding))
		_graph.addEdge(from, to)
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
	
	fun removeEdge(from: Int, index: Int) {
		_graph.edges[from].removeAt(index)
		_edges[from].removeAt(index)
	}
	
	fun setVertexCost(index: Int, cost: Float) {
		_vertices[index]?.text = cost.toString()
		_graph.vertices[index]?.cost = cost
	}
	
	fun setVertexCost(vertex: IUIVertex, cost: Float) {
		val id = _vertices.indexOf(vertex as IUIVertex?)
		if (id != -1) setVertexCost(id, cost)
	}
	
	fun setEdgeCost(from: Int, index: Int, cost: Float) {
		_edges[from][index].text = cost.toString()
		_graph.setCost(from, _edges[from][index].to, cost)
	}
	
	fun setGraphStep(graphStep: GraphStep, algoPaint: AlgoPaint) {
		graphStep.usedVertices.forEach {
			_vertices[it]?.paint = algoPaint.usedPaint
			_vertices[it]?.strokePaint = algoPaint.usedStrokePaint
		}
		graphStep.start.forEach {
			_vertices[it]?.paint = algoPaint.startPaint
			_vertices[it]?.strokePaint = algoPaint.startStrokePaint
		}
		graphStep.end.forEach {
			_vertices[it]?.paint = algoPaint.endPaint
			_vertices[it]?.strokePaint = algoPaint.endStrokePaint
		}
		graphStep.currentVertices.forEach {
			_vertices[it]?.paint = algoPaint.currentPaint
			_vertices[it]?.strokePaint = algoPaint.currentStrokePaint
		}
		graphStep.usedEdges.forEach {usedEdge->
			_edges[usedEdge.from].find { it.to == usedEdge.to}?.strokePaint = algoPaint.usedEdgePaint
			_edges[usedEdge.to].find { it.from == usedEdge.to}?.strokePaint = algoPaint.usedEdgePaint
		}
		graphStep.currentEdges.forEach {currentEdge->
			_edges[currentEdge.from].find { it.to == currentEdge.to}?.strokePaint = algoPaint.currentEdgePaint
			_edges[currentEdge.to].find { it.from == currentEdge.to}?.strokePaint = algoPaint.currentEdgePaint
		}
	}
	
	fun clearGraph(){
		_vertices.forEach {
			it?.paint = vertexPaint
			it?.strokePaint = vertexStrokePaint
		}
		_edges.flatten().forEach {
			it.strokePaint = edgeStrokePaint
		}
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
		val index = findIndex(position, uiGraph)
		return if (index.first == -1) null else uiGraph.edges[index.first][index.second]
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
	
	fun findIndex(position: PointF, uiGraph: UIGraph): Pair<Int, Int> {
		val edges = uiGraph.edges
		for (from in edges.indices.reversed()) {
			for (id in edges[from].indices){
				if (edges[from][id].isEmpty()) continue
				if (position.distTo(edges[from][id]) < radiusSquared) return Pair(from, id)
			}
		}
		return Pair(-1,-1)
	}
}

val emptyPointF = PointF(Float.NaN, Float.NaN)

fun IUIEdge.isEmpty() = startPosition == emptyPointF || endPosition == emptyPointF

fun PointF.distanceSquaredTo(pointF: PointF): Float =
	(pointF.x - x) * (pointF.x - x) + (pointF.y - y) * (pointF.y - y)



