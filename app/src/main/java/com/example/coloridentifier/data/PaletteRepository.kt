package com.example.coloridentifier.data

import android.content.Context
import android.content.SharedPreferences
import com.example.coloridentifier.model.Palette
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * repozytorium zarzadzajace paletami kolorow
 */
class PaletteRepository(context: Context) {

    // instancja sharedpreferences do przechowywania danych
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    // instancja gson do serializacji json
    private val gson = Gson()

    companion object {
        // nazwa pliku sharedpreferences
        private const val PREFS_NAME = "palette_prefs"
        // klucz przechowujacy liste palet
        private const val KEY_PALETTES = "saved_palettes"
    }

    /**
     * zapisuje palete do repozytorium
     */
    fun savePalette(palette: Palette): Boolean {
        // pobiera wszystkie palety jako mutowalna liste
        val palettes = getAllPalettes().toMutableList()
        
        // szuka indeksu palety o tym samym id
        val existingIndex = palettes.indexOfFirst { it.id == palette.id }
        // sprawdza czy paleta juz istnieje
        if (existingIndex != -1) {
            // aktualizuje istniejaca palete
            palettes[existingIndex] = palette
        } else {
            // dodaje nowa palete do listy
            palettes.add(palette)
        }
        
        // zapisuje liste do sharedpreferences
        return savePalettes(palettes)
    }

    /**
     * usuwa palete po identyfikatorze
     */
    fun deletePalette(paletteId: String): Boolean {
        // pobiera wszystkie palety jako mutowalna liste
        val palettes = getAllPalettes().toMutableList()
        // usuwa palete o podanym id
        val removed = palettes.removeIf { it.id == paletteId }
        // zapisuje liste jesli usunieto
        if (removed) {
            savePalettes(palettes)
        }
        // zwraca informacje czy usunieto
        return removed
    }

    /**
     * usuwa konkretny obiekt palety
     */
    fun deletePalette(palette: Palette): Boolean {
        // deleguje do funkcji usuwania po id
        return deletePalette(palette.id)
    }

    /**
     * pobiera wszystkie zapisane palety
     */
    fun getAllPalettes(): List<Palette> {
        // pobiera json z sharedpreferences lub zwraca pusta liste
        val json = sharedPreferences.getString(KEY_PALETTES, null) ?: return emptyList()
        // tworzy typ dla deserializacji listy palet
        val type = object : TypeToken<List<Palette>>() {}.type
        // deserializuje json do listy palet
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            // zwraca pusta liste w przypadku bledu
            emptyList()
        }
    }

    /**
     * pobiera palete po identyfikatorze
     */
    fun getPaletteById(paletteId: String): Palette? {
        // szuka palety o podanym id w liscie
        return getAllPalettes().find { it.id == paletteId }
    }

    /**
     * usuwa wszystkie zapisane palety
     */
    fun clearAllPalettes(): Boolean {
        // usuwa klucz z sharedpreferences
        return sharedPreferences.edit()
            .remove(KEY_PALETTES)
            .commit()
    }

    /**
     * zwraca liczbe zapisanych palet
     */
    fun getPaletteCount(): Int {
        // zwraca rozmiar listy wszystkich palet
        return getAllPalettes().size
    }

    /**
     * aktualizuje nazwe palety
     */
    fun updatePaletteName(paletteId: String, newName: String): Boolean {
        // pobiera wszystkie palety jako mutowalna liste
        val palettes = getAllPalettes().toMutableList()
        // znajduje palete o podanym id
        val palette = palettes.find { it.id == paletteId }
        // sprawdza czy znaleziono palete
        if (palette != null) {
            // ustawia nowa nazwe palety
            palette.name = newName
            // zapisuje zaktualizowana liste
            return savePalettes(palettes)
        }
        // zwraca false jesli nie znaleziono palety
        return false
    }

    /**
     * zapisuje liste palet do sharedpreferences
     */
    private fun savePalettes(palettes: List<Palette>): Boolean {
        // serializuje liste palet do json
        val json = gson.toJson(palettes)
        // zapisuje json w sharedpreferences
        return sharedPreferences.edit()
            .putString(KEY_PALETTES, json)
            .commit()
    }

    /**
     * aktualizuje istniejaca palete
     */
    fun updatePalette(palette: Palette): Boolean {
        // deleguje do funkcji zapisywania palety
        return savePalette(palette)
    }
}
