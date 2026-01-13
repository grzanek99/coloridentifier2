package com.example.coloridentifier.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coloridentifier.data.PaletteRepository
import com.example.coloridentifier.model.Color
import com.example.coloridentifier.model.Palette

/**
 * ViewModel for managing palettes
 */
class PaletteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PaletteRepository(application)
    
    private val _palettes = MutableLiveData<List<Palette>>()
    val palettes: LiveData<List<Palette>> = _palettes

    init {
        loadPalettes()
    }

    /**
     * Loads all saved palettes
     */
    fun loadPalettes() {
        _palettes.value = repository.getAllPalettes()
    }

    /**
     * Saves a new palette
     */
    fun savePalette(palette: Palette): Boolean {
        val result = repository.savePalette(palette)
        if (result) {
            loadPalettes()
        }
        return result
    }

    /**
     * Creates and saves a palette from list of colors
     */
    fun createPalette(name: String, colors: List<Color>): Boolean {
        if (colors.isEmpty() || colors.size > 5) {
            return false
        }
        
        val palette = Palette(
            name = name,
            colors = colors.toMutableList()
        )
        
        return savePalette(palette)
    }

    /**
     * Deletes a palette
     */
    fun deletePalette(palette: Palette): Boolean {
        val result = repository.deletePalette(palette)
        if (result) {
            loadPalettes()
        }
        return result
    }

    /**
     * Clears all palettes
     */
    fun clearAllPalettes(): Boolean {
        val result = repository.clearAllPalettes()
        if (result) {
            loadPalettes()
        }
        return result
    }

    /**
     * Updates palette name
     */
    fun updatePaletteName(paletteId: String, newName: String): Boolean {
        val result = repository.updatePaletteName(paletteId, newName)
        if (result) {
            loadPalettes()
        }
        return result
    }

    /**
     * Updates an existing palette
     */
    fun updatePalette(palette: Palette): Boolean {
        val result = repository.updatePalette(palette)
        if (result) {
            loadPalettes()
        }
        return result
    }

    /**
     * Gets palette count
     */
    fun getPaletteCount(): Int {
        return repository.getPaletteCount()
    }

    /**
     * Gets palette by ID
     */
    fun getPaletteById(paletteId: String): Palette? {
        return repository.getPaletteById(paletteId)
    }
}
