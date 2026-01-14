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
 * adapter do wyswietlania kolorow w recyclerview
 */
class ColorAdapter(
    // lista kolorow do wyswietlenia
    private var colors: List<Color>,
    // callback klikniecia w kolor
    private val onColorClick: (Color) -> Unit,
    // callback dlugiego klikniecia w kolor
    private val onColorLongClick: (Color) -> Unit,
    // funkcja sprawdzajaca czy kolor jest zaznaczony
    private val isColorSelected: (Color) -> Boolean = { false }
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    /**
     * viewholder przechowujacy referencje do widokow elementu
     */
    class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // karta zawierajaca element
        val cardView: CardView = view.findViewById(R.id.colorCard)
        // widok podgladu koloru
        val colorPreview: View = view.findViewById(R.id.colorPreview)
        // textview z nazwa koloru
        val colorName: TextView = view.findViewById(R.id.colorName)
        // textview z wartoscia hex
        val colorHex: TextView = view.findViewById(R.id.colorHex)
        // textview z wartosciami rgb
        val colorRgb: TextView = view.findViewById(R.id.colorRgb)
        // ikona wskaznika zaznaczenia
        val selectionIndicator: ImageView = view.findViewById(R.id.selectionIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        // inflatuje layout elementu z xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color, parent, false)
        // zwraca nowy viewholder z widokiem
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        // pobiera kolor na danej pozycji
        val color = colors[position]
        
        // ustawia kolor tla podgladu
        holder.colorPreview.setBackgroundColor(color.toColorInt())
        
        // ustawia tekst nazwy koloru
        holder.colorName.text = color.name
        // ustawia tekst wartosci hex
        holder.colorHex.text = color.hexValue
        // ustawia tekst wartosci rgb
        holder.colorRgb.text = color.getRGBString()
        
        // pokazuje lub ukrywa wskaznik zaznaczenia
        holder.selectionIndicator.visibility = if (isColorSelected(color)) {
            // widoczny jesli zaznaczony
            View.VISIBLE
        } else {
            // niewidoczny jesli nie zaznaczony
            View.GONE
        }
        
        // ustawia listener klikniecia w karte
        holder.cardView.setOnClickListener {
            // wywoluje callback z kolorem
            onColorClick(color)
        }
        
        // ustawia listener dlugiego klikniecia w karte
        holder.cardView.setOnLongClickListener {
            // wywoluje callback z kolorem
            onColorLongClick(color)
            // zwraca true zeby zaznaczyc ze obsluzono
            true
        }
    }

    override fun getItemCount(): Int = colors.size

    /**
     * aktualizuje liste kolorow
     */
    fun updateColors(newColors: List<Color>) {
        // zastepuje stara liste nowa
        colors = newColors
        // powiadamia adapter o zmianach
        notifyDataSetChanged()
    }
}
