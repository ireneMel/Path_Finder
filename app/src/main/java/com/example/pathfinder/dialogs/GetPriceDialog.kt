package com.example.pathfinder.dialogs

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.example.pathfinder.databinding.BottomSheetContentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GetPriceDialog : BottomSheetDialogFragment() {
	companion object {
		const val RESULT = "RESULT"
	}
	
	private lateinit var binding: BottomSheetContentBinding
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		binding = BottomSheetContentBinding.inflate(inflater)
		binding.doneButton.setOnClickListener { setResult(binding.costInput.text.toString()) }
		binding.costInput.setOnEditorActionListener { _, event, _ ->
			if (event == EditorInfo.IME_ACTION_DONE) {
				setResult(binding.costInput.text.toString())
				true
			} else {
				false
			}
		}
		dialog?.setOnCancelListener { setResult("NaN") }
		return binding.root
	}
	
	private fun setResult(text: String) {
		setFragmentResult(RESULT, bundleOf(RESULT to text))
		dismiss()
	}
	
}