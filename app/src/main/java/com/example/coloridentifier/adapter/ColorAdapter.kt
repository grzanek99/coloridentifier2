package com.example.coloridentifier.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.coloridentifier.R
import com.example.coloridentifier.model.Color
import com.example.coloridentifier.utils.ColorUtils

/**
 * Adapter for displaying colors in RecyclerView
 */
class ColorAdapter(
    private var colors: List<Color>,
    private val onColorClick: (Color) -> Unit,
    private val onColorLongClick: (Color) -> Unit,
    private val isColorSelected: (Color) -> Boolean = { false }
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.colorCard)
        val colorPreview: View = view.findViewById(R.id.colorPreview)
        val colorName: TextView = view.findViewById(R.id.colorName)
        val colorHex: TextView = view.findViewById(R.id.colorHex)
        val colorRgb: TextView = view.findViewById(R.id.colorRgb)
        val selectionIndicator: ImageView = view.findViewById(R.id.selectionIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]
        
        // Set color preview
        holder.colorPreview.setBackgroundColor(color.toColorInt())
        
        // Set text information
        holder.colorName.text = color.name
        holder.colorHex.text = color.hexValue
        holder.colorRgb.text = color.getRGBString()
        
        // Show/hide selection indicator
        holder.selectionIndicator.visibility = if (isColorSelected(color)) {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        // Set click listeners
        holder.cardView.setOnClickListener {
            onColorClick(color)
        }
        
        holder.cardView.setOnLongClickListener {
            onColorLongClick(color)
            true
        }
    }

    override fun getItemCount(): Int = colors.size

    /**
     * Updates the list of colors
     */
    fun updateColors(newColors: List<Color>) {
        colors = newColors
        notifyDataSetChanged()
    }
}
