package com.example.coloridentifier.utils

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * obiekt narzedzowy do operacji na kolorach
 */
object ColorUtils {

    /**
     * konwertuje string hex na wartosci rgb
     */
    fun hexToRgb(hex: String): Triple<Int, Int, Int> {
        // usuwa prefix # jesli istnieje
        val cleanHex = hex.removePrefix("#")
        // konwertuje hex na wartosc int
        val colorInt = cleanHex.toLong(16).toInt()
        // wyciaga skladnik czerwony poprzez przesuniecie bitowe
        val red = (colorInt shr 16) and 0xFF
        // wyciaga skladnik zielony poprzez przesuniecie bitowe
        val green = (colorInt shr 8) and 0xFF
        // wyciaga skladnik niebieski poprzez maskowanie
        val blue = colorInt and 0xFF
        // zwraca triple z wartosciami rgb
        return Triple(red, green, blue)
    }

    /**
     * konwertuje wartosci rgb na string hex
     */
    fun rgbToHex(red: Int, green: Int, blue: Int): String {
        // formatuje skladowe rgb jako hex z prefiksem
        return String.format("#%02X%02X%02X", red, green, blue)
    }

    /**
     * konwertuje rgb na hsv
     */
    fun rgbToHsv(red: Int, green: Int, blue: Int): FloatArray {
        // tworzy tablice na wartosci hsv
        val hsv = FloatArray(3)
        // konwertuje rgb do hsv uzywajac funkcji android
        Color.RGBToHSV(red, green, blue, hsv)
        // zwraca tablice z hue saturation value
        return hsv
    }

    /**
     * konwertuje hsv na rgb
     */
    fun hsvToRgb(hue: Float, saturation: Float, value: Float): Int {
        // konwertuje tablice hsv na wartosc int koloru
        return Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }

    /**
     * pobiera kolor z pozycji katowej dla kola kolorow
     */
    fun getColorFromAngle(angle: Float, saturation: Float = 1f, brightness: Float = 1f): Int {
        // konwertuje kat i parametry na kolor rgb
        return hsvToRgb(angle, saturation, brightness)
    }

    /**
     * oblicza kat od punktu srodkowego
     */
    fun calculateAngle(x: Float, y: Float, centerX: Float, centerY: Float): Float {
        // oblicza roznice x od srodka
        val dx = x - centerX
        // oblicza roznice y od srodka
        val dy = y - centerY
        // oblicza kat w stopniach uzywajac atan2
        var angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
        // normalizuje kat do zakresu 0-360
        if (angle < 0) {
            angle += 360f
        }
        // zwraca kat w stopniach
        return angle
    }

    /**
     * oblicza odleglosc od punktu srodkowego
     */
    fun calculateDistance(x: Float, y: Float, centerX: Float, centerY: Float): Float {
        // oblicza roznice x od srodka
        val dx = x - centerX
        // oblicza roznice y od srodka
        val dy = y - centerY
        // zwraca odleglosc euklidesowa
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    /**
     * pobiera kolor piksela z bitmap na okreslonych wspolrzednych
     */
    fun getPixelColor(bitmap: Bitmap, x: Int, y: Int): Int {
        // sprawdza czy wspolrzedne sa w granicach bitmap
        if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) {
            // zwraca przezroczysty jesli poza granicami
            return Color.TRANSPARENT
        }
        // pobiera i zwraca kolor piksela
        return bitmap.getPixel(x, y)
    }

    /**
     * sprawdza czy kolor jest ciemny
     */
    fun isDarkColor(color: Int): Boolean {
        // oblicza ciemnosc koloru wzorem luminancji
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        // zwraca true jesli ciemnosc wieksza niz 0.5
        return darkness >= 0.5
    }

    /**
     * zwraca kontrastujacy kolor tekstu dla tla
     */
    fun getContrastingTextColor(backgroundColor: Int): Int {
        // sprawdza czy tlo jest ciemne
        return if (isDarkColor(backgroundColor)) {
            // zwraca bialy dla ciemnego tla
            Color.WHITE
        } else {
            // zwraca czarny dla jasnego tla
            Color.BLACK
        }
    }

    /**
     * dostosowuje jasnosc koloru
     */
    fun adjustBrightness(color: Int, factor: Float): Int {
        // tworzy tablice na wartosci hsv
        val hsv = FloatArray(3)
        // konwertuje kolor na hsv
        Color.colorToHSV(color, hsv)
        // mnozy value przez wspolczynnik i ogranicza do 0-1
        hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)
        // konwertuje z powrotem na kolor int
        return Color.HSVToColor(hsv)
    }
}
