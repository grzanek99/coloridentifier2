package com.example.coloridentifier.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coloridentifier.data.ColorRepository
import com.example.coloridentifier.model.Color

/**
 * ViewModel for managing colors
 */
class ColorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ColorRepository(application)
    
    private val _colors = MutableLiveData<List<Color>>()
    val colors: LiveData<List<Color>> = _colors

    private val _selectedColors = MutableLiveData<MutableSet<Color>>(mutableSetOf())
    val selectedColors: LiveData<MutableSet<Color>> = _selectedColors

    init {
        loadColors()
    }

    /**
     * Loads all saved colors
     */
    fun loadColors() {
        _colors.value = repository.getAllColors()
    }

    /**
     * Saves a new color
     */
    fun saveColor(color: Color): Boolean {
        val result = repository.saveColor(color)
        if (result) {
            loadColors()
        }
        return result
    }

    /**
     * Deletes a color
     */
    fun deleteColor(color: Color): Boolean {
        val result = repository.deleteColor(color)
        if (result) {
            // Remove from selection if it was selected
            val currentSelection = _selectedColors.value?.toMutableSet() ?: mutableSetOf()
            currentSelection.remove(color)
            _selectedColors.value = currentSelection
            loadColors()
        }
        return result
    }

    /**
     * Clears all colors
     */
    fun clearAllColors(): Boolean {
        val result = repository.clearAllColors()
        if (result) {
            clearSelection()
            loadColors()
        }
        return result
    }

    /**
     * Toggles color selection
     */
    fun toggleColorSelection(color: Color) {
        val currentSelection = _selectedColors.value?.toMutableSet() ?: mutableSetOf()
        
        if (currentSelection.contains(color)) {
            currentSelection.remove(color)
        } else {
            // Limit to 5 colors
            if (currentSelection.size < 5) {
                currentSelection.add(color)
            }
        }
        
        _selectedColors.value = currentSelection
    }

    /**
     * Checks if a color is selected
     */
    fun isColorSelected(color: Color): Boolean {
        return _selectedColors.value?.contains(color) == true
    }

    /**
     * Gets count of selected colors
     */
    fun getSelectedColorCount(): Int {
        return _selectedColors.value?.size ?: 0
    }

    /**
     * Clears selection
     */
    fun clearSelection() {
        _selectedColors.value = mutableSetOf()
    }

    /**
     * Gets selected colors as list
     */
    fun getSelectedColorsList(): List<Color> {
        return _selectedColors.value?.toList() ?: emptyList()
    }

    /**
     * Gets color count
     */
    fun getColorCount(): Int {
        return repository.getColorCount()
    }
}
