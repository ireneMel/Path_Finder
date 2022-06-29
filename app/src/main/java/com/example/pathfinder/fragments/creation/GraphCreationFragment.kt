package com.example.pathfinder.fragments.creation

import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.pathfinder.R
import com.example.pathfinder.core.serialization.read.ReadGraphFromFile
import com.example.pathfinder.core.serialization.read.ReadState
import com.example.pathfinder.core.serialization.write.FileState
import com.example.pathfinder.core.serialization.write.SaveGraphToFile
import com.example.pathfinder.core.uiGraph.AlgoDesign
import com.example.pathfinder.core.uiGraph.GraphDesign
import com.example.pathfinder.core.uiGraph.UIVertexDesign
import com.example.pathfinder.databinding.FragmentGraphCreationBinding
import com.example.pathfinder.dialogs.GetPriceDialog
import com.example.pathfinder.utils.getThemeColor
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val OPEN_CLICKED = "open_clicked"

class GraphCreationFragment : Fragment(R.layout.fragment_graph_creation) {
	private lateinit var binding: FragmentGraphCreationBinding
	
	companion object {
		/**
		 * Use this factory method to create a new instance of
		 * this fragment using the provided parameters.
		 *
		 * @param isOpenClicked Parameter 1.
		 * @return A new instance of fragment
		 */
		@JvmStatic
		fun newInstance(isOpenClicked: Boolean) =
			GraphCreationFragment().apply {
				arguments = Bundle().apply {
					putBoolean(OPEN_CLICKED, isOpenClicked)
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
	private val secondaryVariantColor by lazy(LazyThreadSafetyMode.NONE) {
		getThemeColor(com.google.android.material.R.attr.colorSecondaryVariant)
	}
	
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
			defaultVertexDesign,
			defaultVertexDesign.strokePaint,
			Paint().apply { color = onPrimaryColor; textSize = 12 * _1dp },
			3 * _1dp
		)
	}
	
	private fun getAlgoDesign(): AlgoDesign {
		val usedColor = getPaint(getThemeColor(androidx.appcompat.R.attr.colorAccent), 3 * _1dp)
		val currentFillColor = getPaint(secondaryColor)
		val currentStrokeColor = getPaint(secondaryVariantColor, 3 * _1dp)
		
		return AlgoDesign(
			startDesign = defaultVertexDesign,
			endDesign = defaultVertexDesign,
			usedDesign = defaultVertexDesign.copy(strokePaint = usedColor),
			currentDesign = defaultVertexDesign.copy(
				paint = currentFillColor, strokePaint = currentStrokeColor
			),
			usedEdgePaint = usedColor,
			currentEdgePaint = currentStrokeColor
		)
	}
	
	private val viewModel: GraphCreationViewModel by viewModels {
		GraphCreationViewModel.Factory(getDesign())
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentGraphCreationBinding.bind(view)
		
		lifecycleScope.launchWhenStarted {
			viewModel.state.collect(::bindState)
		}
		
		with(binding){
			addVertex.setOnClickListener { viewModel.setEditState(EditState.ADD_VERTEX) }
			addEdge.setOnClickListener { viewModel.setEditState(EditState.ADD_EDGE) }
			setPrice.setOnClickListener { viewModel.setEditState(EditState.SET) }
			remove.setOnClickListener { viewModel.setEditState(EditState.REMOVE) }
			open.setOnClickListener { graphReader.openFile() }
			save.setOnClickListener {
				val graph = binding.graph.graph
				if (graph != null) {
					graphSaver.createFile(graph)
				} else {
					makeToast("The canvas must not be empty")
				}
			}
		}
		//load graph when corresponding button was clicked
		if (requireArguments().getBoolean(OPEN_CLICKED)) {
			graphReader.openFile()
			requireArguments().putBoolean(OPEN_CLICKED, false)
		}
		
		
		lifecycleScope.launchWhenStarted {
			graphReader.state.collect {
				when (it) {
					ReadState.ERROR       -> makeToast("Error while reading file.")
					is ReadState.FINISHED -> {
						viewModel.loadGraph(it.graph)
						makeToast("Success")
					}
					else                  -> {}
				}
			}
		}
		
		lifecycleScope.launchWhenStarted {
			graphSaver.state.collect {
				when (it) {
					FileState.ERROR  -> makeToast("Error while saving file.")
					FileState.CLOSED -> makeToast("Success")
					else             -> {}
				}
			}
		}
	}
	
	private suspend fun bindState(state: State){
		with(binding.graph) {
			drawMode = state.combinedMode
			touchMode = state.combinedMode
			if (state is State.Edit) {
				graph = state.uiGraph
			}
		}
		if (state is State.SetPrice){
			viewModel.setPrice(getPrice())
		}
	}
	
	private val dialog = GetPriceDialog()
	private suspend fun getPrice(): Float = suspendCoroutine {
		dialog.show(childFragmentManager, null)
		dialog.setFragmentResultListener(GetPriceDialog.RESULT) { _, bundle ->
			val result = bundle.getString(GetPriceDialog.RESULT)
			it.resume(
				try {
					result!!.toFloat()
				} catch (_: Exception) {
					Float.NaN
				}
			)
		}
	}
	
	private val graphSaver: SaveGraphToFile by lazy {
		SaveGraphToFile(
			requireActivity().activityResultRegistry,
			viewLifecycleOwner,
			requireActivity().contentResolver
		)
	}
	
	private val graphReader: ReadGraphFromFile by lazy {
		ReadGraphFromFile(
			requireActivity().activityResultRegistry,
			viewLifecycleOwner,
			requireActivity().contentResolver
		)
	}
	
	private fun makeToast(text: String) =
		Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
	
}