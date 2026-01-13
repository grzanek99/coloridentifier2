package com.example.coloridentifier.utils

import android.graphics.Color
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Utility object for mapping RGB values to color names
 */
object ColorNameMapper {

    private data class NamedColor(
        val name: String,
        val red: Int,
        val green: Int,
        val blue: Int
    )

    // Comprehensive list of named colors with RGB values
    private val namedColors = listOf(
        // Basic colors
        NamedColor("Czerwony", 255, 0, 0),
        NamedColor("Zielony", 0, 255, 0),
        NamedColor("Niebieski", 0, 0, 255),
        NamedColor("Żółty", 255, 255, 0),
        NamedColor("Cyjan", 0, 255, 255),
        NamedColor("Magenta", 255, 0, 255),
        NamedColor("Biały", 255, 255, 255),
        NamedColor("Czarny", 0, 0, 0),
        NamedColor("Szary", 128, 128, 128),
        
        // Shades of red
        NamedColor("Ciemnoczerwony", 139, 0, 0),
        NamedColor("Karmazynowy", 220, 20, 60),
        NamedColor("Koralowy", 255, 127, 80),
        NamedColor("Łososiowy", 250, 128, 114),
        NamedColor("Różowy", 255, 192, 203),
        NamedColor("Ciemnoróżowy", 255, 20, 147),
        
        // Shades of orange
        NamedColor("Pomarańczowy", 255, 165, 0),
        NamedColor("Ciemnopomarańczowy", 255, 140, 0),
        NamedColor("Pomidorowy", 255, 99, 71),
        
        // Shades of yellow
        NamedColor("Złoty", 255, 215, 0),
        NamedColor("Khaki", 240, 230, 140),
        NamedColor("Beżowy", 245, 245, 220),
        
        // Shades of green
        NamedColor("Limonkowy", 50, 205, 50),
        NamedColor("Ciemnozielony", 0, 100, 0),
        NamedColor("Oliwkowy", 128, 128, 0),
        NamedColor("Żółtozielony", 154, 205, 50),
        NamedColor("Morski", 46, 139, 87),
        NamedColor("Miętowy", 152, 251, 152),
        NamedColor("Wiosennozielony", 0, 255, 127),
        
        // Shades of cyan
        NamedColor("Turkusowy", 64, 224, 208),
        NamedColor("Akwamaryna", 127, 255, 212),
        NamedColor("Jasnoniebieski", 173, 216, 230),
        
        // Shades of blue
        NamedColor("Ciemnoniebieski", 0, 0, 139),
        NamedColor("Królewskimodrý", 65, 105, 225),
        NamedColor("Stalowy", 70, 130, 180),
        NamedColor("Błękitny", 135, 206, 235),
        NamedColor("Granatowy", 0, 0, 128),
        NamedColor("Chabrowy", 75, 0, 130),
        
        // Shades of purple
        NamedColor("Fioletowy", 128, 0, 128),
        NamedColor("Ciemnofioletowy", 148, 0, 211),
        NamedColor("Lawendowy", 230, 230, 250),
        NamedColor("Śliwkowy", 221, 160, 221),
        NamedColor("Orchidea", 218, 112, 214),
        
        // Shades of brown
        NamedColor("Brązowy", 165, 42, 42),
        NamedColor("Czekoladowy", 210, 105, 30),
        NamedColor("Piaskowy", 244, 164, 96),
        NamedColor("Siennowy", 160, 82, 45),
        NamedColor("Kasztanowy", 205, 92, 92),
        
        // Shades of gray
        NamedColor("Jasnoszary", 211, 211, 211),
        NamedColor("Srebro", 192, 192, 192),
        NamedColor("Ciemnoszary", 169, 169, 169),
        NamedColor("Grafitowy", 64, 64, 64),
        
        // Special colors
        NamedColor("Kremowy", 255, 255, 240),
        NamedColor("Bursztynowy", 255, 191, 0)
    )

    /**
     * Finds the closest color name for given RGB values
     * Uses Euclidean distance in RGB color space
     */
    fun getColorName(red: Int, green: Int, blue: Int): String {
        var minDistance = Double.MAX_VALUE
        var closestColor = "Nieznany"

        for (namedColor in namedColors) {
            val distance = calculateColorDistance(
                red, green, blue,
                namedColor.red, namedColor.green, namedColor.blue
            )
            
            if (distance < minDistance) {
                minDistance = distance
                closestColor = namedColor.name
            }
        }

        return closestColor
    }

    /**
     * Finds the closest color name for given color int
     */
    fun getColorName(colorInt: Int): String {
        val red = Color.red(colorInt)
        val green = Color.green(colorInt)
        val blue = Color.blue(colorInt)
        return getColorName(red, green, blue)
    }

    /**
     * Calculates Euclidean distance between two colors in RGB space
     */
    private fun calculateColorDistance(
        r1: Int, g1: Int, b1: Int,
        r2: Int, g2: Int, b2: Int
    ): Double {
        val rDiff = (r1 - r2).toDouble()
        val gDiff = (g1 - g2).toDouble()
        val bDiff = (b1 - b2).toDouble()
        return sqrt(rDiff.pow(2) + gDiff.pow(2) + bDiff.pow(2))
    }
}
