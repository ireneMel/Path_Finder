package com.example.pathfinder.customization.recyclerview

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.customization.ThemeClickListener
import com.example.pathfinder.customization.ThemeNames
import com.example.pathfinder.databinding.ThemeItemBinding
import com.example.pathfinder.models.ThemeModel

class ThemeListAdapter(
	private val listener: ThemeClickListener, private val themeList: List<ThemeModel>
) : RecyclerView.Adapter<ThemeListAdapter.ThemeViewHolder>() {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
		return ThemeViewHolder(
			ThemeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)
	}
	
	override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
		val item = themeList[position]
		
		with(holder.binding) {
			title.text = item.themeName
			themeContainer.setOnClickListener {
				listener.onChosen(
					themeList[holder.adapterPosition].themeName
				)
			} //customize item in recyclerview themself
			configureBackgroundColors(item, themeContainer)
			configureIconColors(item, previewImage)
		}
	}
	
	override fun getItemCount(): Int {
		return themeList.size
	}
	
	private fun configureBackgroundColors(item: ThemeModel, cardView: CardView) {
		when (item.themeName) {
			ThemeNames.BASIC   -> setColor(R.color.green_200, cardView)
			ThemeNames.SEA     -> setColor(R.color.blue, cardView)
			ThemeNames.SPRING  -> setColor(R.color.green, cardView)
			ThemeNames.GRAPE   -> setColor(R.color.light_pink, cardView)
			ThemeNames.DRACULA -> setColor(R.color.red, cardView)
		}
	}
	
	private fun configureIconColors(item: ThemeModel, imageView: ImageView) {
		when (item.themeName) {
			ThemeNames.BASIC   -> setImageColor(R.color.green_600, imageView)
			ThemeNames.SEA     -> setImageColor(R.color.dark_blue, imageView)
			ThemeNames.SPRING  -> setImageColor(R.color.spring_violet, imageView)
			ThemeNames.GRAPE   -> setImageColor(R.color.blue_grape, imageView)
			ThemeNames.DRACULA -> setImageColor(R.color.dark_red, imageView)
		}
	}
	
	private fun setColor(@ColorRes color: Int, cardView: CardView) {
		cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, color))
	}
	
	private fun setImageColor(@ColorRes color: Int, imageView: ImageView) {
		imageView.imageTintList =
			ColorStateList.valueOf(ContextCompat.getColor(imageView.context, color))
	}
	
	class ThemeViewHolder(val binding: ThemeItemBinding) : RecyclerView.ViewHolder(binding.root)
}