package com.example.pathfinder.customization.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.customization.ThemeClickListener
import com.example.pathfinder.customization.ThemeNames
import com.example.pathfinder.models.ThemeModel

class ThemeListAdapter(
    private val context: Context,
    private val listener: ThemeClickListener
) : RecyclerView.Adapter<ThemeListAdapter.ThemeViewHolder>() {

    private var themeList = emptyList<ThemeModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        return ThemeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.theme_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val item = themeList[position]

        holder.name.text = item.themeName
        holder.cardView.setOnClickListener {
            listener.onChosen(
                themeList[holder.adapterPosition].themeName
            )
        }

        //customize item in recyclerview themself
        configureColors(item, holder.cardView)
    }

    override fun getItemCount(): Int {
        return themeList.size
    }

    fun submitList(themeList: List<ThemeModel>) {
        this.themeList = themeList
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
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, color))
    }

    class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val cardView: CardView

        init {
            cardView = itemView.findViewById(R.id.theme_container)
            name = itemView.findViewById(R.id.title);
        }
    }
}