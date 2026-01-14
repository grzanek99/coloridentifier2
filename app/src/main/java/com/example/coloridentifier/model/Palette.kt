package com.example.coloridentifier.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

/**
 * model reprezentujacy palete kolorow
 */
@Parcelize
data class Palette(
    // unikalny identyfikator palety
    val id: String = UUID.randomUUID().toString(),
    // nazwa palety edytowalna
    var name: String,
    // data utworzenia palety
    val createdDate: Date = Date(),
    // lista kolorow w palecie maksymalnie 5
    val colors: MutableList<Color> = mutableListOf()
) : Parcelable {

    init {
        // waliduje czy nazwa nie jest pusta
        require(name.isNotBlank()) { "Palette name cannot be blank" }
    }

    /**
     * dodaje kolor do palety, maksymalnie 5 kolorow
     */
    fun addColor(color: Color): Boolean {
        // sprawdza czy paleta ma juz 5 kolorow
        if (colors.size >= 5) {
            // zwraca false jesli paleta pelna
            return false
        }
        // dodaje kolor do listy
        colors.add(color)
        // zwraca true po udanym dodaniu
        return true
    }

    /**
     * usuwa kolor z palety
     */
    fun removeColor(color: Color): Boolean {
        // usuwa kolor z listy i zwraca wynik
        return colors.remove(color)
    }

    /**
     * usuwa kolor z konkretnej pozycji
     */
    fun removeColorAt(position: Int): Color? {
        // sprawdza czy pozycja jest poprawna
        if (position < 0 || position >= colors.size) {
            // zwraca null dla niepoprawnej pozycji
            return null
        }
        // usuwa i zwraca kolor z danej pozycji
        return colors.removeAt(position)
    }

    /**
     * zwraca liczbe kolorow w palecie
     */
    fun getColorCount(): Int {
        // zwraca rozmiar listy kolorow
        return colors.size
    }

    /**
     * sprawdza czy paleta jest pelna
     */
    fun isFull(): Boolean {
        // zwraca true jesli ma 5 lub wiecej kolorow
        return colors.size >= 5
    }

    /**
     * sprawdza czy paleta jest pusta
     */
    fun isEmpty(): Boolean {
        // zwraca true jesli lista kolorow pusta
        return colors.isEmpty()
    }

    /**
     * zwraca reprezentacje palety w formacie json
     */
    fun toJsonString(): String {
        // laczy kolory w sformatowane stringi json
        val colorsList = colors.joinToString(",\n    ") { color ->
            """{"name": "${color.name}", "hex": "${color.hexValue}", "rgb": "${color.getRGBString()}"}"""
        }
        // zwraca kompletny json z metadanymi palety
        return """
{
  "id": "$id",
  "name": "$name",
  "createdDate": "${createdDate}",
  "colors": [
    $colorsList
  ]
}
        """.trimIndent()
    }

    /**
     * zwraca reprezentacje palety jako tekst
     */
    fun toTextString(): String {
        // laczy kolory w linie tekstowe
        val colorsText = colors.joinToString("\n") { color ->
            "${color.name}: ${color.getHEXString()} - ${color.getRGBString()}"
        }
        // zwraca sformatowany tekst z metadanymi
        return """
Palette: $name
Created: $createdDate
Colors:
$colorsText
        """.trimIndent()
    }

    /**
     * zwraca krotki opis palety
     */
    fun getDescription(): String {
        // laczy nazwe z liczba kolorow
        return "$name (${colors.size} colors)"
    }
}
