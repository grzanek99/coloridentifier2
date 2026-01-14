package com.example.coloridentifier.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coloridentifier.data.ColorRepository
import com.example.coloridentifier.model.Color

/**
 * viewmodel zarzadzajacy kolorami
 */
class ColorViewModel(application: Application) : AndroidViewModel(application) {

    // instancja repozytorium kolorow
    private val repository = ColorRepository(application)
    
    // mutable livedata z lista kolorow
    private val _colors = MutableLiveData<List<Color>>()
    // publiczne livedata z lista kolorow
    val colors: LiveData<List<Color>> = _colors

    // mutable livedata z zaznaczonymi kolorami
    private val _selectedColors = MutableLiveData<MutableSet<Color>>(mutableSetOf())
    // publiczne livedata z zaznaczonymi kolorami
    val selectedColors: LiveData<MutableSet<Color>> = _selectedColors

    init {
        // laduje kolory podczas inicjalizacji
        loadColors()
    }

    /**
     * laduje wszystkie zapisane kolory
     */
    fun loadColors() {
        // pobiera kolory z repozytorium i ustawia w livedata
        _colors.value = repository.getAllColors()
    }

    /**
     * zapisuje nowy kolor
     */
    fun saveColor(color: Color): Boolean {
        // zapisuje kolor w repozytorium
        val result = repository.saveColor(color)
        // prz zaznaczony w ustawieniach reload colors
        if (result) {
            // przeladowuje liste kolorow
            loadColors()
        }
        // zwraca wynik operacji
        return result
    }

    /**
     * usuwa kolor
     */
    fun deleteColor(color: Color): Boolean {
        // usuwa kolor z repozytorium
        val result = repository.deleteColor(color)
        // sprawdza czy operacja sie powiodla
        if (result) {
            // pobiera aktualna selekcje jako mutowalny set
            val currentSelection = _selectedColors.value?.toMutableSet() ?: mutableSetOf()
            // usuwa kolor z selekcji
            currentSelection.remove(color)
            // aktualizuje livedata z selekcja
            _selectedColors.value = currentSelection
            // przeladowuje liste kolorow
            loadColors()
        }
        // zwraca wynik operacji
        return result
    }

    /**
     * usuwa wszystkie kolory
     */
    fun clearAllColors(): Boolean {
        // usuwa wszystkie kolory z repozytorium
        val result = repository.clearAllColors()
        // sprawdza czy operacja sie powiodla
        if (result) {
            // czysci selekcje
            clearSelection()
            // przeladowuje liste kolorow
            loadColors()
        }
        // zwraca wynik operacji
        return result
    }

    /**
     * przelacza zaznaczenie koloru
     */
    fun toggleColorSelection(color: Color) {
        // pobiera aktualna selekcje jako mutowalny set
        val currentSelection = _selectedColors.value?.toMutableSet() ?: mutableSetOf()
        
        // sprawdza czy kolor jest juz zaznaczony
        if (currentSelection.contains(color)) {
            // usuwa kolor z selekcji
            currentSelection.remove(color)
        } else {
            // sprawdza czy nie przekroczono limitu
            if (currentSelection.size < 5) {
                // dodaje kolor do selekcji
                currentSelection.add(color)
            }
        }
        
        // aktualizuje livedata z selekcja
        _selectedColors.value = currentSelection
    }

    /**
     * sprawdza czy kolor jest zaznaczony
     */
    fun isColorSelected(color: Color): Boolean {
        // zwraca czy kolor jest w selekcji
        return _selectedColors.value?.contains(color) == true
    }

    /**
     * zwraca liczbe zaznaczonych kolorow
     */
    fun getSelectedColorCount(): Int {
        // zwraca rozmiar setu lub 0
        return _selectedColors.value?.size ?: 0
    }

    /**
     * czysci selekcje
     */
    fun clearSelection() {
        // ustawia pusty set w livedata
        _selectedColors.value = mutableSetOf()
    }

    /**
     * zwraca zaznaczone kolory jako liste
     */
    fun getSelectedColorsList(): List<Color> {
        // konwertuje set na liste lub zwraca pusta liste
        return _selectedColors.value?.toList() ?: emptyList()
    }

    /**
     * zwraca liczbe wszystkich kolorow
     */
    fun getColorCount(): Int {
        // pobiera liczbe kolorow z repozytorium
        return repository.getColorCount()
    }
}
