package com.example.pathfinder.fragments.graph

import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.example.pathfinder.R
import com.example.pathfinder.core.uiGraph.AlgoDesign
import com.example.pathfinder.core.uiGraph.GraphDesign
import com.example.pathfinder.core.uiGraph.UIVertexDesign
import com.example.pathfinder.databinding.FragmentGraphBinding
import com.example.pathfinder.fragments.graph.creation.GraphCreationFragment
import com.example.pathfinder.utils.getThemeColor

private const val OPEN_CLICKED = "open_clicked"
private const val GRAPH_TYPE = "graph_type"

class GraphFragment : Fragment(R.layout.fragment_graph) {
	private lateinit var binding: FragmentGraphBinding
	val viewModel: GraphViewModel by viewModels {
		GraphViewModel.Factory(getDesign(), getAlgoDesign())
	}
	
	companion object {
		@JvmStatic
		fun newInstance(isOpenClicked: Boolean, isBiDirectional: Boolean = false) =
			GraphFragment().apply {
				arguments = Bundle().apply {
					putBoolean(OPEN_CLICKED, isOpenClicked)
					putBoolean(GRAPH_TYPE, isBiDirectional)
				}
			}
	}
	
	private fun getPaint(color: Int, strokeWidth: Float? = null) = Paint().apply {
		this.color = color
		if (strokeWidth != null) this.strokeWidth = strokeWidth
	}
	
	private val primaryColor by lazy(LazyThreadSafetyMode.NONE) { getThemeColor(com.google.android.material.R.attr.colorPrimary) }
	private val primaryVariantColor by lazy(LazyThreadSafetyMode.NONE) { getThemeColor(com.google.android.material.R.attr.colorPrimaryVariant) }
	private val onPrimaryColor by lazy(LazyThreadSafetyMode.NONE) { getThemeColor(com.google.android.material.R.attr.colorOnPrimary) }
	private val secondaryColor by lazy(LazyThreadSafetyMode.NONE) { getThemeColor(com.google.android.material.R.attr.colorSecondary) }
	private val secondaryVariantColor by lazy(LazyThreadSafetyMode.NONE) { getThemeColor(com.google.android.material.R.attr.colorSecondaryVariant) }
	
	private val _1dp by lazy(LazyThreadSafetyMode.NONE) { resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT }
	private val defaultVertexDesign by lazy(LazyThreadSafetyMode.NONE) {
		UIVertexDesign(
			radius = 16 * _1dp,
			paint = getPaint(primaryColor),
			strokePaint = getPaint(primaryVariantColor, 3 * _1dp)
		)
	}
	
	private fun getDesign(): GraphDesign {
		return GraphDesign(
			vertexDesign = defaultVertexDesign,
			edgeStrokePaint = defaultVertexDesign.strokePaint,
			textEdgePaint = Paint().apply { color = secondaryVariantColor; textSize = 12 * _1dp },
			textVertexPaint = Paint().apply { color = onPrimaryColor; textSize = 12 * _1dp },
			textPadding = 3 * _1dp
		)
	}
	
	private fun getAlgoDesign(): AlgoDesign {
		val usedColor = getPaint(getThemeColor(androidx.appcompat.R.attr.colorAccent), 3 * _1dp)
		val currentFillColor = getPaint(secondaryColor)
		val currentStrokeColor = getPaint(secondaryVariantColor, 3 * _1dp)
		
		return AlgoDesign(
			startDesign = defaultVertexDesign.copy(strokePaint = getPaint(requireContext().getColor(R.color.teal_200), 3 * _1dp)),
			endDesign = defaultVertexDesign.copy(strokePaint = getPaint(requireContext().getColor(R.color.purple_500), 3 * _1dp)),
			usedDesign = defaultVertexDesign.copy(strokePaint = usedColor),
			currentDesign = defaultVertexDesign.copy(
				paint = currentFillColor, strokePaint = currentStrokeColor
			),
			usedEdgePaint = usedColor,
			currentEdgePaint = currentStrokeColor
		)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentGraphBinding.bind(view)
		if (savedInstanceState == null) childFragmentManager.commit {
			add(
				R.id.graphContainer, GraphCreationFragment.newInstance(
					requireArguments().getBoolean(OPEN_CLICKED),
					requireArguments().getBoolean(GRAPH_TYPE)
				), "Create"
			)
		}
	}
}