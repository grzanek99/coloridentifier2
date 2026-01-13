package com.example.coloridentifier.data

import android.content.Context
import android.content.SharedPreferences
import com.example.coloridentifier.model.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repository for managing saved colors using SharedPreferences and Gson
 */
class ColorRepository(context: Context) {

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "color_prefs"
        private const val KEY_COLORS = "saved_colors"
    }

    /**
     * Saves a color to the repository
     */
    fun saveColor(color: Color): Boolean {
        val colors = getAllColors().toMutableList()
        
        // Check if color already exists (by RGB values)
        val exists = colors.any { it.red == color.red && it.green == color.green && it.blue == color.blue }
        if (exists) {
            return false
        }

        colors.add(color)
        return saveColors(colors)
    }

    /**
     * Deletes a color by ID
     */
    fun deleteColor(colorId: String): Boolean {
        val colors = getAllColors().toMutableList()
        val removed = colors.removeIf { it.id == colorId }
        if (removed) {
            saveColors(colors)
        }
        return removed
    }

    /**
     * Deletes a color object
     */
    fun deleteColor(color: Color): Boolean {
        return deleteColor(color.id)
    }

    /**
     * Gets all saved colors
     */
    fun getAllColors(): List<Color> {
        val json = sharedPreferences.getString(KEY_COLORS, null) ?: return emptyList()
        val type = object : TypeToken<List<Color>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Gets a color by ID
     */
    fun getColorById(colorId: String): Color? {
        return getAllColors().find { it.id == colorId }
    }

    /**
     * Clears all saved colors
     */
    fun clearAllColors(): Boolean {
        return sharedPreferences.edit()
            .remove(KEY_COLORS)
            .commit()
    }

    /**
     * Gets count of saved colors
     */
    fun getColorCount(): Int {
        return getAllColors().size
    }

    /**
     * Checks if a color exists (by RGB values)
     */
    fun colorExists(red: Int, green: Int, blue: Int): Boolean {
        return getAllColors().any { it.red == red && it.green == green && it.blue == blue }
    }

    /**
     * Saves list of colors to SharedPreferences
     */
    private fun saveColors(colors: List<Color>): Boolean {
        val json = gson.toJson(colors)
        return sharedPreferences.edit()
            .putString(KEY_COLORS, json)
            .commit()
    }

    /**
     * Updates an existing color
     */
    fun updateColor(color: Color): Boolean {
        val colors = getAllColors().toMutableList()
        val index = colors.indexOfFirst { it.id == color.id }
        if (index != -1) {
            colors[index] = color
            return saveColors(colors)
        }
        return false
    }
}
