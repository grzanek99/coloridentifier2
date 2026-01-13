package com.example.coloridentifier.data

import android.content.Context
import android.content.SharedPreferences
import com.example.coloridentifier.model.Palette
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repository for managing color palettes using SharedPreferences and Gson
 */
class PaletteRepository(context: Context) {

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "palette_prefs"
        private const val KEY_PALETTES = "saved_palettes"
    }

    /**
     * Saves a palette to the repository
     */
    fun savePalette(palette: Palette): Boolean {
        val palettes = getAllPalettes().toMutableList()
        
        // Check if palette with same ID already exists, update it
        val existingIndex = palettes.indexOfFirst { it.id == palette.id }
        if (existingIndex != -1) {
            palettes[existingIndex] = palette
        } else {
            palettes.add(palette)
        }
        
        return savePalettes(palettes)
    }

    /**
     * Deletes a palette by ID
     */
    fun deletePalette(paletteId: String): Boolean {
        val palettes = getAllPalettes().toMutableList()
        val removed = palettes.removeIf { it.id == paletteId }
        if (removed) {
            savePalettes(palettes)
        }
        return removed
    }

    /**
     * Deletes a palette object
     */
    fun deletePalette(palette: Palette): Boolean {
        return deletePalette(palette.id)
    }

    /**
     * Gets all saved palettes
     */
    fun getAllPalettes(): List<Palette> {
        val json = sharedPreferences.getString(KEY_PALETTES, null) ?: return emptyList()
        val type = object : TypeToken<List<Palette>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Gets a palette by ID
     */
    fun getPaletteById(paletteId: String): Palette? {
        return getAllPalettes().find { it.id == paletteId }
    }

    /**
     * Clears all saved palettes
     */
    fun clearAllPalettes(): Boolean {
        return sharedPreferences.edit()
            .remove(KEY_PALETTES)
            .commit()
    }

    /**
     * Gets count of saved palettes
     */
    fun getPaletteCount(): Int {
        return getAllPalettes().size
    }

    /**
     * Updates palette name
     */
    fun updatePaletteName(paletteId: String, newName: String): Boolean {
        val palettes = getAllPalettes().toMutableList()
        val palette = palettes.find { it.id == paletteId }
        if (palette != null) {
            palette.name = newName
            return savePalettes(palettes)
        }
        return false
    }

    /**
     * Saves list of palettes to SharedPreferences
     */
    private fun savePalettes(palettes: List<Palette>): Boolean {
        val json = gson.toJson(palettes)
        return sharedPreferences.edit()
            .putString(KEY_PALETTES, json)
            .commit()
    }

    /**
     * Updates an existing palette
     */
    fun updatePalette(palette: Palette): Boolean {
        return savePalette(palette)
    }
}
