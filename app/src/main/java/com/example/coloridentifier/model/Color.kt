package com.example.coloridentifier.model

import android.graphics.Color as AndroidColor
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

/**
 * model reprezentujacy pojedynczy kolor w aplikacji
 */
@Parcelize
data class Color(
    // unikalny identyfikator koloru generowany automatycznie
    val id: String = UUID.randomUUID().toString(),
    // nazwa koloru np. "lime green"
    val name: String,
    // skladnik czerwony koloru (0-255)
    val red: Int,
    // skladnik zielony koloru (0-255)
    val green: Int,
    // skladnik niebieski koloru (0-255)
    val blue: Int,
    // wartosc koloru w formacie hex np. "#32cd32"
    val hexValue: String,
    // data i czas zapisania koloru
    val savedDate: Date = Date()
) : Parcelable {

    init {
        // waliduje zakres wartosci czerwonej
        require(red in 0..255) { "Red value must be between 0 and 255" }
        // waliduje zakres wartosci zielonej
        require(green in 0..255) { "Green value must be between 0 and 255" }
        // waliduje zakres wartosci niebieskiej
        require(blue in 0..255) { "Blue value must be between 0 and 255" }
    }

    /**
     * zwraca string z wartosciami rgb w formacie "rgb(r, g, b)"
     */
    fun getRGBString(): String {
        // laczy skladowe rgb w sformatowany string
        return "RGB($red, $green, $blue)"
    }

    /**
     * zwraca wartosc hex koloru
     */
    fun getHEXString(): String {
        // zwraca zapisana wartosc hexadecymalna
        return hexValue
    }

    /**
     * konwertuje skladowe rgb na wartosc int android
     */
    fun toColorInt(): Int {
        // laczy skladowe rgb w pojedyncza wartosc int
        return AndroidColor.rgb(red, green, blue)
    }

    /**
     * zwraca pelny opis koloru z nazwa, rgb i hex
     */
    fun getFullDescription(): String {
        // laczy nazwe, rgb i hex w jeden string
        return "$name - ${getRGBString()} - ${getHEXString()}"
    }

    companion object {
        /**
         * tworzy obiekt color z wartosci int android
         */
        fun fromColorInt(colorInt: Int, name: String = "Unknown"): Color {
            // wyciaga skladnik czerwony z int
            val red = AndroidColor.red(colorInt)
            // wyciaga skladnik zielony z int
            val green = AndroidColor.green(colorInt)
            // wyciaga skladnik niebieski z int
            val blue = AndroidColor.blue(colorInt)
            // formatuje wartosc hex z skladowych rgb
            val hex = String.format("#%02X%02X%02X", red, green, blue)
            // tworzy i zwraca nowy obiekt color
            return Color(
                name = name,
                red = red,
                green = green,
                blue = blue,
                hexValue = hex
            )
        }

        /**
         * tworzy obiekt color z wartosci hexadecymalnej
         */
        fun fromHex(hexString: String, name: String = "Unknown"): Color {
            // usuwa prefix # jesli istnieje
            val hex = hexString.removePrefix("#")
            // konwertuje hex na wartosc int
            val colorInt = hex.toLong(16).toInt()
            // wyciaga skladnik czerwony poprzez przesuniecie bitowe
            val red = (colorInt shr 16) and 0xFF
            // wyciaga skladnik zielony poprzez przesuniecie bitowe
            val green = (colorInt shr 8) and 0xFF
            // wyciaga skladnik niebieski poprzez maskowanie
            val blue = colorInt and 0xFF
            // tworzy i zwraca nowy obiekt color
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
