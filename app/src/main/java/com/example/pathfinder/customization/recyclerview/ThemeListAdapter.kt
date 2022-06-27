package com.example.pathfinder.customization.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.customization.ThemeClickListener
import com.example.pathfinder.customization.ThemeNames
import com.example.pathfinder.databinding.ThemeItemBinding
import com.example.pathfinder.models.ThemeModel

class ThemeListAdapter(
    private val listener: ThemeClickListener,
    private val themeList: List<ThemeModel>
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
            }
            //customize item in recyclerview themself
            configureColors(item, themeContainer)
        }
    }

    override fun getItemCount(): Int {
        return themeList.size
    }

    private fun configureColors(item: ThemeModel, cardView: CardView) {
        when (item.themeName) {
            ThemeNames.BASIC -> setColor(R.color.purple_200, cardView)
            ThemeNames.SEA -> setColor(R.color.blue, cardView)
            ThemeNames.SPRING -> setColor(R.color.green, cardView)
            ThemeNames.GRAPE -> setColor(R.color.light_pink, cardView)
            ThemeNames.DRACULA -> setColor(R.color.red, cardView)
        }
    }

    private fun setColor(color: Int, cardView: CardView) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, color))
    }

    class ThemeViewHolder(val binding: ThemeItemBinding) : RecyclerView.ViewHolder(binding.root)
}