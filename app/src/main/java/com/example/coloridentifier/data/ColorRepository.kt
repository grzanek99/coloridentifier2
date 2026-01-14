package com.example.coloridentifier.data

import android.content.Context
import android.content.SharedPreferences
import com.example.coloridentifier.model.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * repozytorium zarzadzajace zapisanymi kolorami
 */
class ColorRepository(context: Context) {

    // instancja sharedpreferences do przechowywania danych
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    // instancja gson do serializacji json
    private val gson = Gson()

    companion object {
        // nazwa pliku sharedpreferences
        private const val PREFS_NAME = "color_prefs"
        // klucz przechowujacy liste kolorow
        private const val KEY_COLORS = "saved_colors"
    }

    /**
     * zapisuje kolor do repozytorium
     */
    fun saveColor(color: Color): Boolean {
        // pobiera wszystkie kolory jako mutowalna liste
        val colors = getAllColors().toMutableList()
        
        // sprawdza czy kolor juz istnieje po wartosciach rgb
        val exists = colors.any { it.red == color.red && it.green == color.green && it.blue == color.blue }
        // zwraca false jesli kolor juz istnieje
        if (exists) {
            return false
        }

        // dodaje nowy kolor do listy
        colors.add(color)
        // zapisuje liste do sharedpreferences
        return saveColors(colors)
    }

    /**
     * usuwa kolor po identyfikatorze
     */
    fun deleteColor(colorId: String): Boolean {
        // pobiera wszystkie kolory jako mutowalna liste
        val colors = getAllColors().toMutableList()
        // usuwa kolor o podanym id
        val removed = colors.removeIf { it.id == colorId }
        // zapisuje liste jesli usunieto
        if (removed) {
            saveColors(colors)
        }
        // zwraca informacje czy usunieto
        return removed
    }

    /**
     * usuwa konkretny obiekt koloru
     */
    fun deleteColor(color: Color): Boolean {
        // deleguje do funkcji usuwania po id
        return deleteColor(color.id)
    }

    /**
     * pobiera wszystkie zapisane kolory
     */
    fun getAllColors(): List<Color> {
        // pobiera json z sharedpreferences lub zwraca pusta liste
        val json = sharedPreferences.getString(KEY_COLORS, null) ?: return emptyList()
        // tworzy typ dla deserializacji listy kolorow
        val type = object : TypeToken<List<Color>>() {}.type
        // deserializuje json do listy kolorow
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            // zwraca pusta liste w przypadku bledu
            emptyList()
        }
    }

    /**
     * pobiera kolor po identyfikatorze
     */
    fun getColorById(colorId: String): Color? {
        // szuka koloru o podanym id w liscie
        return getAllColors().find { it.id == colorId }
    }

    /**
     * usuwa wszystkie zapisane kolory
     */
    fun clearAllColors(): Boolean {
        // usuwa klucz z sharedpreferences
        return sharedPreferences.edit()
            .remove(KEY_COLORS)
            .commit()
    }

    /**
     * zwraca liczbe zapisanych kolorow
     */
    fun getColorCount(): Int {
        // zwraca rozmiar listy wszystkich kolorow
        return getAllColors().size
    }

    /**
     * sprawdza czy kolor istnieje po wartosciach rgb
     */
    fun colorExists(red: Int, green: Int, blue: Int): Boolean {
        // szuka koloru o podanych wartosciach rgb
        return getAllColors().any { it.red == red && it.green == green && it.blue == blue }
    }

    /**
     * zapisuje liste kolorow do sharedpreferences
     */
    private fun saveColors(colors: List<Color>): Boolean {
        // serializuje liste kolorow do json
        val json = gson.toJson(colors)
        // zapisuje json w sharedpreferences
        return sharedPreferences.edit()
            .putString(KEY_COLORS, json)
            .commit()
    }

    /**
     * aktualizuje istniejacy kolor
     */
    fun updateColor(color: Color): Boolean {
        // pobiera wszystkie kolory jako mutowalna liste
        val colors = getAllColors().toMutableList()
        // znajduje indeks koloru o tym samym id
        val index = colors.indexOfFirst { it.id == color.id }
        // sprawdza czy znaleziono kolor
        if (index != -1) {
            // zastepuje stary kolor nowym
            colors[index] = color
            // zapisuje zaktualizowana liste
            return saveColors(colors)
        }
        // zwraca false jesli nie znaleziono koloru
        return false
    }
}
