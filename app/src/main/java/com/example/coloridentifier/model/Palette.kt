package com.example.coloridentifier.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

@Parcelize
data class Palette(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    val createdDate: Date = Date(),
    val colors: MutableList<Color> = mutableListOf()
) : Parcelable {

    init {
        require(name.isNotBlank()) { "Palette name cannot be blank" }
    }

    /**
     * Adds a color to the palette
     * Maximum 5 colors allowed
     */
    fun addColor(color: Color): Boolean {
        if (colors.size >= 5) {
            return false
        }
        colors.add(color)
        return true
    }

    /**
     * Removes a color from the palette
     */
    fun removeColor(color: Color): Boolean {
        return colors.remove(color)
    }

    /**
     * Removes a color at specific position
     */
    fun removeColorAt(position: Int): Color? {
        if (position < 0 || position >= colors.size) {
            return null
        }
        return colors.removeAt(position)
    }

    /**
     * Returns the number of colors in the palette
     */
    fun getColorCount(): Int {
        return colors.size
    }

    /**
     * Checks if the palette is full (5 colors)
     */
    fun isFull(): Boolean {
        return colors.size >= 5
    }

    /**
     * Checks if the palette is empty
     */
    fun isEmpty(): Boolean {
        return colors.isEmpty()
    }

    /**
     * Returns a JSON representation of the palette
     */
    fun toJsonString(): String {
        val colorsList = colors.joinToString(",\n    ") { color ->
            """{"name": "${color.name}", "hex": "${color.hexValue}", "rgb": "${color.getRGBString()}"}"""
        }
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
     * Returns a text representation of the palette
     */
    fun toTextString(): String {
        val colorsText = colors.joinToString("\n") { color ->
            "${color.name}: ${color.getHEXString()} - ${color.getRGBString()}"
        }
        return """
Palette: $name
Created: $createdDate
Colors:
$colorsText
        """.trimIndent()
    }

    /**
     * Returns a description of the palette
     */
    fun getDescription(): String {
        return "$name (${colors.size} colors)"
    }
}
