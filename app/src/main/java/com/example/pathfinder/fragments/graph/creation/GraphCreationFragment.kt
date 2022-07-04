package com.example.pathfinder.fragments.graph.creation

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.codertainment.materialintro.utils.materialIntroSequence
import com.example.pathfinder.R
import com.example.pathfinder.core.serialization.read.ReadGraphFromFile
import com.example.pathfinder.core.serialization.read.ReadState
import com.example.pathfinder.core.serialization.write.FileState
import com.example.pathfinder.core.serialization.write.SaveGraphToFile
import com.example.pathfinder.databinding.FragmentGraphCreationBinding
import com.example.pathfinder.dialogs.GetPriceDialog
import com.example.pathfinder.fragments.graph.GraphFragment
import com.example.pathfinder.fragments.graph.visualization.GraphVisualizationFragment
import com.example.pathfinder.utils.Hints.basicConfig
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
        fun newInstance(isOpenClicked: Boolean) = GraphCreationFragment().apply {
            arguments = Bundle().apply {
                putBoolean(OPEN_CLICKED, isOpenClicked)
            }
        }
    }

    private val viewModel: GraphCreationViewModel by viewModels {
        GraphCreationViewModel.Factory((requireParentFragment() as GraphFragment).viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGraphCreationBinding.bind(view)

        setHasOptionsMenu(true)

        lifecycleScope.launchWhenStarted {
            viewModel.state.collect(::bindState)
        }

        with(binding) {
            play.setOnClickListener {
                if(graph.graph?.vertices?.isEmpty() == true) {
                    makeToast("The canvas must not be empty")
                } else {
                    parentFragmentManager.commit {
                        replace(R.id.graphContainer, GraphVisualizationFragment())
                        addToBackStack(null)
                    }
                }
            }
            addVertex.setOnClickListener { viewModel.setEditState(Action.ADD_VERTEX) }
            addEdge.setOnClickListener { viewModel.setEditState(Action.ADD_EDGE) }
            setPrice.setOnClickListener { viewModel.setEditState(Action.SET) }
            remove.setOnClickListener { viewModel.setEditState(Action.REMOVE) }
        }
        if (requireArguments().getBoolean(OPEN_CLICKED)) {
            graphReader.openFile()
            requireArguments().putBoolean(OPEN_CLICKED, false)
        }

        lifecycleScope.launchWhenStarted {
            graphReader.state.collect {
                when (it) {
                    ReadState.ERROR -> makeToast("Error while reading file.")
                    is ReadState.FINISHED -> {
                        viewModel.loadGraph(it.graph)
                        makeToast("Success")
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            graphSaver.state.collect {
                when (it) {
                    FileState.ERROR -> makeToast("Error while saving file.")
                    FileState.CLOSED -> makeToast("Success")
                    else -> {}
                }
            }
        }
    }

    private suspend fun bindState(state: State) {
        with(binding.graph) {
            drawMode = state.combinedMode
            touchMode = state.combinedMode
            if (state is State.Edit) {
                graph = state.uiGraph
            }
        }
        if (state is State.SetPrice) {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vizualization, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_to_file -> {
                val graph = binding.graph.graph
                if (graph != null) {
                    graphSaver.createFile(graph)
                } else {
                    makeToast("The canvas must not be empty")
                }
                return true
            }
            R.id.open_from_file -> {
                graphReader.openFile()
                return true
            }

            R.id.help_visualize -> {
                materialIntroSequence(200, showSkip = true) {
                    addConfig {basicConfig(
                            binding.addVertex,
                            "Click here to enter creating vertex mode")}
                    addConfig {basicConfig(
                            binding.addEdge,
                            "Click here to enter creating edge mode")}
                    addConfig { basicConfig(binding.remove, "Click here to enter removing mode") }
                    addConfig {basicConfig(
                            binding.setPrice,
                            "Click here to set prices for edges or vertices")}
                    addConfig {basicConfig(
                            binding.play,
                            "Click here to visualise the algorithm of finding the best path")}
                    addConfig {basicConfig(
                            binding.graph,
                            "After choosing the mode by clicking buttons below, click on this area to create graph")}
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
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