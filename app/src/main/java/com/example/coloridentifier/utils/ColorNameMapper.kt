package com.example.coloridentifier.utils

import android.graphics.Color
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * obiekt narzedzowy mapujacy wartosci rgb na nazwy kolorow
 */
object ColorNameMapper {

    // klasa przechowujaca nazwe koloru z wartosciami rgb
    private data class NamedColor(
        // nazwa koloru
        val name: String,
        // skladnik czerwony 0-255
        val red: Int,
        // skladnik zielony 0-255
        val green: Int,
        // skladnik niebieski 0-255
        val blue: Int
    )

    // kompletna lista nazwanych kolorow z wartosciami rgb
    private val namedColors = listOf(
        // kolory podstawowe
        NamedColor("Red", 255, 0, 0),
        NamedColor("Green", 0, 255, 0),
        NamedColor("Blue", 0, 0, 255),
        NamedColor("Yellow", 255, 255, 0),
        NamedColor("Cyan", 0, 255, 255),
        NamedColor("Magenta", 255, 0, 255),
        NamedColor("White", 255, 255, 255),
        NamedColor("Black", 0, 0, 0),
        NamedColor("Gray", 128, 128, 128),
        
        // odcienie czerwieni
        NamedColor("Dark Red", 139, 0, 0),
        NamedColor("Crimson", 220, 20, 60),
        NamedColor("Coral", 255, 127, 80),
        NamedColor("Salmon", 250, 128, 114),
        NamedColor("Pink", 255, 192, 203),
        NamedColor("Deep Pink", 255, 20, 147),
        
        // odcienie pomaranczowego
        NamedColor("Orange", 255, 165, 0),
        NamedColor("Dark Orange", 255, 140, 0),
        NamedColor("Tomato", 255, 99, 71),
        
        // odcienie zoltego
        NamedColor("Gold", 255, 215, 0),
        NamedColor("Khaki", 240, 230, 140),
        NamedColor("Beige", 245, 245, 220),
        
        // odcienie zielonego
        NamedColor("Lime", 50, 205, 50),
        NamedColor("Dark Green", 0, 100, 0),
        NamedColor("Olive", 128, 128, 0),
        NamedColor("Yellow Green", 154, 205, 50),
        NamedColor("Sea Green", 46, 139, 87),
        NamedColor("Mint", 152, 251, 152),
        NamedColor("Spring Green", 0, 255, 127),
        
        // odcienie cyjanowego
        NamedColor("Turquoise", 64, 224, 208),
        NamedColor("Aquamarine", 127, 255, 212),
        NamedColor("Light Blue", 173, 216, 230),
        
        // odcienie niebieskiego
        NamedColor("Dark Blue", 0, 0, 139),
        NamedColor("Royal Blue", 65, 105, 225),
        NamedColor("Steel Blue", 70, 130, 180),
        NamedColor("Sky Blue", 135, 206, 235),
        NamedColor("Navy", 0, 0, 128),
        NamedColor("Indigo", 75, 0, 130),
        
        // odcienie fioletu
        NamedColor("Purple", 128, 0, 128),
        NamedColor("Dark Violet", 148, 0, 211),
        NamedColor("Lavender", 230, 230, 250),
        NamedColor("Plum", 221, 160, 221),
        NamedColor("Orchid", 218, 112, 214),
        
        // odcienie brazowego
        NamedColor("Brown", 165, 42, 42),
        NamedColor("Chocolate", 210, 105, 30),
        NamedColor("Sandy Brown", 244, 164, 96),
        NamedColor("Sienna", 160, 82, 45),
        NamedColor("Rosy Brown", 205, 92, 92),
        
        // odcienie szarosci
        NamedColor("Light Gray", 211, 211, 211),
        NamedColor("Silver", 192, 192, 192),
        NamedColor("Dark Gray", 169, 169, 169),
        NamedColor("Dim Gray", 64, 64, 64),
        
        // kolory specjalne
        NamedColor("Ivory", 255, 255, 240),
        NamedColor("Amber", 255, 191, 0)
    )

    /**
     * znajduje najblizsza nazwe koloru dla podanych wartosci rgb
     */
    fun getColorName(red: Int, green: Int, blue: Int): String {
        // inicjalizuje minimalna odleglosc maksymalna wartoscia
        var minDistance = Double.MAX_VALUE
        // domyslna nazwa gdy nie znaleziono dopasowania
        var closestColor = "Unknown"

        // iteruje przez wszystkie kolory w bazie
        for (namedColor in namedColors) {
            // oblicza odleglosc euklidesowa w przestrzeni rgb
            val distance = calculateColorDistance(
                red, green, blue,
                namedColor.red, namedColor.green, namedColor.blue
            )
            
            // sprawdza czy znaleziono blizszy kolor
            if (distance < minDistance) {
                // aktualizuje minimalna odleglosc
                minDistance = distance
                // zapisuje nazwe najblizszego koloru
                closestColor = namedColor.name
            }
        }

        // zwraca nazwe najbardziej podobnego koloru
        return closestColor
    }

    /**
     * znajduje najblizsza nazwe koloru dla wartosci int
     */
    fun getColorName(colorInt: Int): String {
        // wyciaga skladnik czerwony z int
        val red = Color.red(colorInt)
        // wyciaga skladnik zielony z int
        val green = Color.green(colorInt)
        // wyciaga skladnik niebieski z int
        val blue = Color.blue(colorInt)
        // deleguje do funkcji dla wartosci rgb
        return getColorName(red, green, blue)
    }

    /**
     * oblicza odleglosc euklidesowa miedzy dwoma kolorami w przestrzeni rgb
     */
    private fun calculateColorDistance(
        r1: Int, g1: Int, b1: Int,
        r2: Int, g2: Int, b2: Int
    ): Double {
        // oblicza roznice skladowej czerwonej
        val rDiff = (r1 - r2).toDouble()
        // oblicza roznice skladowej zielonej
        val gDiff = (g1 - g2).toDouble()
        // oblicza roznice skladowej niebieskiej
        val bDiff = (b1 - b2).toDouble()
        // zwraca pierwiastek sumy kwadratow roznic
        return sqrt(rDiff.pow(2) + gDiff.pow(2) + bDiff.pow(2))
    }
}
