package com.example.coloridentifier.model

import android.graphics.Color as AndroidColor
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

@Parcelize
data class Color(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val red: Int,
    val green: Int,
    val blue: Int,
    val hexValue: String,
    val savedDate: Date = Date()
) : Parcelable {

    init {
        require(red in 0..255) { "Red value must be between 0 and 255" }
        require(green in 0..255) { "Green value must be between 0 and 255" }
        require(blue in 0..255) { "Blue value must be between 0 and 255" }
    }

    /**
     * Returns RGB string representation
     * Example: "RGB(255, 128, 0)"
     */
    fun getRGBString(): String {
        return "RGB($red, $green, $blue)"
    }

    /**
     * Returns HEX string representation
     * Example: "#FF8000"
     */
    fun getHEXString(): String {
        return hexValue
    }

    /**
     * Converts RGB values to Android Color int
     */
    fun toColorInt(): Int {
        return AndroidColor.rgb(red, green, blue)
    }

    /**
     * Returns full description of the color
     * Example: "Limonkowy - RGB(50, 205, 50) - #32CD32"
     */
    fun getFullDescription(): String {
        return "$name - ${getRGBString()} - ${getHEXString()}"
    }

    companion object {
        /**
         * Creates a Color object from Android Color int
         */
        fun fromColorInt(colorInt: Int, name: String = "Unknown"): Color {
            val red = AndroidColor.red(colorInt)
            val green = AndroidColor.green(colorInt)
            val blue = AndroidColor.blue(colorInt)
            val hex = String.format("#%02X%02X%02X", red, green, blue)
            return Color(
                name = name,
                red = red,
                green = green,
                blue = blue,
                hexValue = hex
            )
        }

        /**
         * Creates a Color object from hex string
         */
        fun fromHex(hexString: String, name: String = "Unknown"): Color {
            val hex = hexString.removePrefix("#")
            val colorInt = hex.toLong(16).toInt()
            val red = (colorInt shr 16) and 0xFF
            val green = (colorInt shr 8) and 0xFF
            val blue = colorInt and 0xFF
            return Color(
                name = name,
                red = red,
                green = green,
                blue = blue,
                hexValue = "#$hex".uppercase()
            )
        }
    }
}
