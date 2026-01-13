package com.example.coloridentifier.utils

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utility object for color-related operations
 */
object ColorUtils {

    /**
     * Converts hex string to RGB values
     * @param hex Hex color string (with or without #)
     * @return Triple of (red, green, blue)
     */
    fun hexToRgb(hex: String): Triple<Int, Int, Int> {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16).toInt()
        val red = (colorInt shr 16) and 0xFF
        val green = (colorInt shr 8) and 0xFF
        val blue = colorInt and 0xFF
        return Triple(red, green, blue)
    }

    /**
     * Converts RGB values to hex string
     * @return Hex color string with # prefix
     */
    fun rgbToHex(red: Int, green: Int, blue: Int): String {
        return String.format("#%02X%02X%02X", red, green, blue)
    }

    /**
     * Converts RGB to HSV (Hue, Saturation, Value)
     * @return FloatArray of [h, s, v]
     */
    fun rgbToHsv(red: Int, green: Int, blue: Int): FloatArray {
        val hsv = FloatArray(3)
        Color.RGBToHSV(red, green, blue, hsv)
        return hsv
    }

    /**
     * Converts HSV to RGB
     * @param hue Hue (0-360)
     * @param saturation Saturation (0-1)
     * @param value Value/Brightness (0-1)
     * @return Color int
     */
    fun hsvToRgb(hue: Float, saturation: Float, value: Float): Int {
        return Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }

    /**
     * Gets color from circular position (for color wheel)
     * @param angle Angle in degrees (0-360)
     * @param saturation Saturation (0-1)
     * @param brightness Brightness (0-1)
     * @return Color int
     */
    fun getColorFromAngle(angle: Float, saturation: Float = 1f, brightness: Float = 1f): Int {
        return hsvToRgb(angle, saturation, brightness)
    }

    /**
     * Calculates angle from center point
     * @param x X coordinate
     * @param y Y coordinate
     * @param centerX Center X
     * @param centerY Center Y
     * @return Angle in degrees (0-360)
     */
    fun calculateAngle(x: Float, y: Float, centerX: Float, centerY: Float): Float {
        val dx = x - centerX
        val dy = y - centerY
        var angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
        if (angle < 0) {
            angle += 360f
        }
        return angle
    }

    /**
     * Calculates distance from center
     */
    fun calculateDistance(x: Float, y: Float, centerX: Float, centerY: Float): Float {
        val dx = x - centerX
        val dy = y - centerY
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    /**
     * Gets pixel color from bitmap at specified coordinates
     */
    fun getPixelColor(bitmap: Bitmap, x: Int, y: Int): Int {
        if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) {
            return Color.TRANSPARENT
        }
        return bitmap.getPixel(x, y)
    }

    /**
     * Checks if color is dark (for determining text color)
     */
    fun isDarkColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    /**
     * Gets contrasting text color (black or white) for given background color
     */
    fun getContrastingTextColor(backgroundColor: Int): Int {
        return if (isDarkColor(backgroundColor)) {
            Color.WHITE
        } else {
            Color.BLACK
        }
    }

    /**
     * Adjusts brightness of a color
     * @param color Original color
     * @param factor Brightness factor (0-1 for darker, 1+ for brighter)
     */
    fun adjustBrightness(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)
        return Color.HSVToColor(hsv)
    }
}
