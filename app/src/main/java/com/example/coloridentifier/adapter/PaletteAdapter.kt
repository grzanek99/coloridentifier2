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
 * adapter do wyswietlania palet w recyclerview
 */
class PaletteAdapter(
    // lista palet do wyswietlenia
    private var palettes: List<Palette>,
    // callback klikniecia w palete
    private val onPaletteClick: (Palette) -> Unit,
    // callback dlugiego klikniecia w palete
    private val onPaletteLongClick: (Palette) -> Unit
) : RecyclerView.Adapter<PaletteAdapter.PaletteViewHolder>() {

    // format daty do wyswietlania
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * viewholder przechowujacy referencje do widokow elementu
     */
    class PaletteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // karta zawierajaca element
        val cardView: CardView = view.findViewById(R.id.paletteCard)
        // textview z nazwa palety
        val paletteName: TextView = view.findViewById(R.id.paletteInfo)
        // textview z informacjami o palecie
        val paletteInfo: TextView = view.findViewById(R.id.paletteInfo)
        // slot na pierwszy kolor
        val colorSlot1: View = view.findViewById(R.id.colorSlot1)
        // slot na drugi kolor
        val colorSlot2: View = view.findViewById(R.id.colorSlot2)
        // slot na trzeci kolor
        val colorSlot3: View = view.findViewById(R.id.colorSlot3)
        // slot na czwarty kolor
        val colorSlot4: View = view.findViewById(R.id.colorSlot4)
        // slot na piaty kolor
        val colorSlot5: View = view.findViewById(R.id.colorSlot5)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaletteViewHolder {
        // inflatuje layout elementu z xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_palette, parent, false)
        // zwraca nowy viewholder z widokiem
        return PaletteViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaletteViewHolder, position: Int) {
        // pobiera palete na danej pozycji
        val palette = palettes[position]
        
        // ustawia tekst nazwy palety
        holder.paletteName.text = palette.name
        // ustawia tekst informacji z liczba kolorow i data
        holder.paletteInfo.text = "${palette.colors.size} colors • ${dateFormat.format(palette.createdDate)}"
        
        // tworzy liste slotow kolorow
        val slots = listOf(
            holder.colorSlot1, 
            holder.colorSlot2, 
            holder.colorSlot3, 
            holder.colorSlot4, 
            holder.colorSlot5
        )
        
        // iteruje przez sloty z indeksem
        slots.forEachIndexed { index, slot ->
            // sprawdza czy paleta ma kolor na tym indeksie
            if (index < palette.colors.size) {
                // ustawia kolor tla slotu
                slot.setBackgroundColor(palette.colors[index].toColorInt())
                // ustawia slot jako widoczny
                slot.visibility = View.VISIBLE
            } else {
                // ustawia slot jako niewidoczny dla pustych slotow
                slot.visibility = View.INVISIBLE
            }
        }
        
        // ustawia listener klikniecia w karte
        holder.cardView.setOnClickListener {
            // wywoluje callback z paleta
            onPaletteClick(palette)
        }
        
        // ustawia listener dlugiego klikniecia w karte
        holder.cardView.setOnLongClickListener {
            // wywoluje callback z paleta
            onPaletteLongClick(palette)
            // zwraca true zeby zaznaczyc ze obsluzono
            true
        }
    }

    override fun getItemCount(): Int = palettes.size

    /**
     * aktualizuje liste palet
     */
    fun updatePalettes(newPalettes: List<Palette>) {
        // zastepuje stara liste nowa
        palettes = newPalettes
        // powiadamia adapter o zmianach
        notifyDataSetChanged()
    }
}
