package com.example.coloridentifier.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.coloridentifier.R
import com.example.coloridentifier.model.Palette
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter for displaying palettes in RecyclerView
 */
class PaletteAdapter(
    private var palettes: List<Palette>,
    private val onPaletteClick: (Palette) -> Unit,
    private val onPaletteLongClick: (Palette) -> Unit
) : RecyclerView.Adapter<PaletteAdapter.PaletteViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    class PaletteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.paletteCard)
        val paletteName: TextView = view.findViewById(R.id.paletteName)
        val paletteInfo: TextView = view.findViewById(R.id.paletteInfo)
        val colorSlot1: View = view.findViewById(R.id.colorSlot1)
        val colorSlot2: View = view.findViewById(R.id.colorSlot2)
        val colorSlot3: View = view.findViewById(R.id.colorSlot3)
        val colorSlot4: View = view.findViewById(R.id.colorSlot4)
        val colorSlot5: View = view.findViewById(R.id.colorSlot5)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaletteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_palette, parent, false)
        return PaletteViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaletteViewHolder, position: Int) {
        val palette = palettes[position]
        
        holder.paletteName.text = palette.name
        holder.paletteInfo.text = "${palette.colors.size} colors • ${dateFormat.format(palette.createdDate)}"
        
        // Set color slots
        val slots = listOf(
            holder.colorSlot1, 
            holder.colorSlot2, 
            holder.colorSlot3, 
            holder.colorSlot4, 
            holder.colorSlot5
        )
        
        slots.forEachIndexed { index, slot ->
            if (index < palette.colors.size) {
                slot.setBackgroundColor(palette.colors[index].toColorInt())
                slot.visibility = View.VISIBLE
            } else {
                slot.visibility = View.INVISIBLE
            }
        }
        
        // Set click listeners
        holder.cardView.setOnClickListener {
            onPaletteClick(palette)
        }
        
        holder.cardView.setOnLongClickListener {
            onPaletteLongClick(palette)
            true
        }
    }

    override fun getItemCount(): Int = palettes.size

    /**
     * Updates the list of palettes
     */
    fun updatePalettes(newPalettes: List<Palette>) {
        palettes = newPalettes
        notifyDataSetChanged()
    }
}
