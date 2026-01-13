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
        NamedColor("Red", 255, 0, 0),
        NamedColor("Green", 0, 255, 0),
        NamedColor("Blue", 0, 0, 255),
        NamedColor("Yellow", 255, 255, 0),
        NamedColor("Cyan", 0, 255, 255),
        NamedColor("Magenta", 255, 0, 255),
        NamedColor("White", 255, 255, 255),
        NamedColor("Black", 0, 0, 0),
        NamedColor("Gray", 128, 128, 128),
        
        // Shades of red
        NamedColor("Dark Red", 139, 0, 0),
        NamedColor("Crimson", 220, 20, 60),
        NamedColor("Coral", 255, 127, 80),
        NamedColor("Salmon", 250, 128, 114),
        NamedColor("Pink", 255, 192, 203),
        NamedColor("Deep Pink", 255, 20, 147),
        
        // Shades of orange
        NamedColor("Orange", 255, 165, 0),
        NamedColor("Dark Orange", 255, 140, 0),
        NamedColor("Tomato", 255, 99, 71),
        
        // Shades of yellow
        NamedColor("Gold", 255, 215, 0),
        NamedColor("Khaki", 240, 230, 140),
        NamedColor("Beige", 245, 245, 220),
        
        // Shades of green
        NamedColor("Lime", 50, 205, 50),
        NamedColor("Dark Green", 0, 100, 0),
        NamedColor("Olive", 128, 128, 0),
        NamedColor("Yellow Green", 154, 205, 50),
        NamedColor("Sea Green", 46, 139, 87),
        NamedColor("Mint", 152, 251, 152),
        NamedColor("Spring Green", 0, 255, 127),
        
        // Shades of cyan
        NamedColor("Turquoise", 64, 224, 208),
        NamedColor("Aquamarine", 127, 255, 212),
        NamedColor("Light Blue", 173, 216, 230),
        
        // Shades of blue
        NamedColor("Dark Blue", 0, 0, 139),
        NamedColor("Royal Blue", 65, 105, 225),
        NamedColor("Steel Blue", 70, 130, 180),
        NamedColor("Sky Blue", 135, 206, 235),
        NamedColor("Navy", 0, 0, 128),
        NamedColor("Indigo", 75, 0, 130),
        
        // Shades of purple
        NamedColor("Purple", 128, 0, 128),
        NamedColor("Dark Violet", 148, 0, 211),
        NamedColor("Lavender", 230, 230, 250),
        NamedColor("Plum", 221, 160, 221),
        NamedColor("Orchid", 218, 112, 214),
        
        // Shades of brown
        NamedColor("Brown", 165, 42, 42),
        NamedColor("Chocolate", 210, 105, 30),
        NamedColor("Sandy Brown", 244, 164, 96),
        NamedColor("Sienna", 160, 82, 45),
        NamedColor("Rosy Brown", 205, 92, 92),
        
        // Shades of gray
        NamedColor("Light Gray", 211, 211, 211),
        NamedColor("Silver", 192, 192, 192),
        NamedColor("Dark Gray", 169, 169, 169),
        NamedColor("Dim Gray", 64, 64, 64),
        
        // Special colors
        NamedColor("Ivory", 255, 255, 240),
        NamedColor("Amber", 255, 191, 0)
    )

    /**
     * Finds the closest color name for given RGB values
     * Uses Euclidean distance in RGB color space
     */
    fun getColorName(red: Int, green: Int, blue: Int): String {
        var minDistance = Double.MAX_VALUE
        var closestColor = "Unknown"

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
