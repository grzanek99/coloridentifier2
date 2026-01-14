package com.example.coloridentifier.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coloridentifier.data.PaletteRepository
import com.example.coloridentifier.model.Color
import com.example.coloridentifier.model.Palette

/**
 * viewmodel zarzadzajacy paletami
 */
class PaletteViewModel(application: Application) : AndroidViewModel(application) {

    // instancja repozytorium palet
    private val repository = PaletteRepository(application)
    
    // mutable livedata z lista palet
    private val _palettes = MutableLiveData<List<Palette>>()
    // publiczne livedata z lista palet
    val palettes: LiveData<List<Palette>> = _palettes

    init {
        // laduje palety podczas inicjalizacji
        loadPalettes()
    }

    /**
     * laduje wszystkie zapisane palety
     */
    fun loadPalettes() {
        // pobiera palety z repozytorium i ustawia w livedata
        _palettes.value = repository.getAllPalettes()
    }

    /**
     * zapisuje nowa palete
     */
    fun savePalette(palette: Palette): Boolean {
        // zapisuje palete w repozytorium
        val result = repository.savePalette(palette)
        // sprawdza czy operacja sie powiodla
        if (result) {
            // przeladowuje liste palet
            loadPalettes()
        }
        // zwraca wynik operacji
        return result
    }

    /**
     * tworzy i zapisuje palete z listy kolorow
     */
    fun createPalette(name: String, colors: List<Color>): Boolean {
        // waliduje liczbe kolorow
        if (colors.isEmpty() || colors.size > 5) {
            // zwraca false dla niepoprawnej liczby
            return false
        }
        
        // tworzy nowy obiekt palety
        val palette = Palette(
            name = name,
            colors = colors.toMutableList()
        )
        
        // zapisuje palete
        return savePalette(palette)
    }

    /**
     * usuwa palete
     */
    fun deletePalette(palette: Palette): Boolean {
        // usuwa palete z repozytorium
        val result = repository.deletePalette(palette)
        // sprawdza czy operacja sie powiodla
        if (result) {
            // przeladowuje liste palet
            loadPalettes()
        }
        // zwraca wynik operacji
        return result
    }

    /**
     * usuwa wszystkie palety
     */
    fun clearAllPalettes(): Boolean {
        // usuwa wszystkie palety z repozytorium
        val result = repository.clearAllPalettes()
        // sprawdza czy operacja sie powiodla
        if (result) {
            // przeladowuje liste palet
            loadPalettes()
        }
        // zwraca wynik operacji
        return result
    }

    /**
     * aktualizuje nazwe palety
     */
    fun updatePaletteName(paletteId: String, newName: String): Boolean {
        // aktualizuje nazwe w repozytorium
        val result = repository.updatePaletteName(paletteId, newName)
        // sprawdza czy operacja sie powiodla
        if (result) {
            // przeladowuje liste palet
            loadPalettes()
        }
        // zwraca wynik operacji
        return result
    }

    /**
     * aktualizuje istniejaca palete
     */
    fun updatePalette(palette: Palette): Boolean {
        // aktualizuje palete w repozytorium
        val result = repository.updatePalette(palette)
        // sprawdza czy operacja sie powiodla
        if (result) {
            // przeladowuje liste palet
            loadPalettes()
        }
        // zwraca wynik operacji
        return result
    }

    /**
     * zwraca liczbe wszystkich palet
     */
    fun getPaletteCount(): Int {
        // pobiera liczbe palet z repozytorium
        return repository.getPaletteCount()
    }

    /**
     * pobiera palete po identyfikatorze
     */
    fun getPaletteById(paletteId: String): Palette? {
        // szuka i zwraca palete z repozytorium
        return repository.getPaletteById(paletteId)
    }
}
