package com.example.coloridentifier.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.coloridentifier.model.Color
import com.example.coloridentifier.model.Palette
import java.io.File
import java.io.FileOutputStream

/**
 * obiekt narzedzowy do udostepniania i operacji na schowku
 */
object ShareUtils {

    /**
     * kopiuje tekst do schowka systemowego
     */
    fun copyToClipboard(context: Context, label: String, text: String) {
        // pobiera serwis schowka
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // tworzy clip z tekstem
        val clip = ClipData.newPlainText(label, text)
        // ustawia clip jako podstawowy w schowku
        clipboard.setPrimaryClip(clip)
    }

    /**
     * udostepnia kolor jako tekst
     */
    fun shareColorAsText(context: Context, color: Color) {
        // tworzy sformatowany tekst z informacjami o kolorze
        val shareText = """
            Color: ${color.name}
            HEX: ${color.hexValue}
            RGB: ${color.getRGBString()}
        """.trimIndent()

        // tworzy intent do udostepniania
        val intent = Intent(Intent.ACTION_SEND).apply {
            // ustawia typ jako tekst
            type = "text/plain"
            // dodaje temat wiadomosci
            putExtra(Intent.EXTRA_SUBJECT, "Color: ${color.name}")
            // dodaje tresc do udostepnienia
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        // uruchamia chooser do wyboru aplikacji
        context.startActivity(Intent.createChooser(intent, "Share Color"))
    }

    /**
     * udostepnia kolor jako obraz
     */
    fun shareColorAsImage(context: Context, color: Color) {
        // tworzy bitmap jednolitego koloru
        val bitmap = ImageUtils.createColorBitmap(color.toColorInt(), 500, 500)
        // deleguje do funkcji udostepniania bitmap
        shareBitmap(context, bitmap, "color_${color.name.replace(" ", "_")}.png")
    }

    /**
     * udostepnia palete jako tekst
     */
    fun sharePaletteAsText(context: Context, palette: Palette) {
        // konwertuje palete na string tekstowy
        val shareText = palette.toTextString()
        
        // tworzy intent do udostepniania
        val intent = Intent(Intent.ACTION_SEND).apply {
            // ustawia typ jako tekst
            type = "text/plain"
            // dodaje temat wiadomosci
            putExtra(Intent.EXTRA_SUBJECT, "Palette: ${palette.name}")
            // dodaje tresc do udostepnienia
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        // uruchamia chooser do wyboru aplikacji
        context.startActivity(Intent.createChooser(intent, "Share Palette"))
    }

    /**
     * udostepnia palete jako json
     */
    fun sharePaletteAsJson(context: Context, palette: Palette) {
        // konwertuje palete na string json
        val jsonText = palette.toJsonString()
        
        // tworzy intent do udostepniania
        val intent = Intent(Intent.ACTION_SEND).apply {
            // ustawia typ jako json
            type = "application/json"
            // dodaje temat wiadomosci
            putExtra(Intent.EXTRA_SUBJECT, "Palette: ${palette.name}")
            // dodaje tresc do udostepnienia
            putExtra(Intent.EXTRA_TEXT, jsonText)
        }
        // uruchamia chooser do wyboru aplikacji
        context.startActivity(Intent.createChooser(intent, "Share Palette JSON"))
    }

    /**
     * udostepnia palete jako obraz z kolorami obok siebie
     */
    fun sharePaletteAsImage(context: Context, palette: Palette) {
        // pobiera liczbe kolorow w palecie
        val colorCount = palette.colors.size
        // sprawdza czy paleta ma kolory
        if (colorCount == 0) return

        // ustawia szerokosc pojedynczego koloru
        val colorWidth = 200
        // oblicza calkowita szerokosc obrazu
        val totalWidth = colorWidth * colorCount
        // ustawia wysokosc obrazu
        val height = 500

        // tworzy bitmap o obliczonych wymiarach
        val bitmap = Bitmap.createBitmap(totalWidth, height, Bitmap.Config.ARGB_8888)
        // tworzy canvas do rysowania na bitmap
        val canvas = android.graphics.Canvas(bitmap)

        // iteruje przez kolory w palecie
        palette.colors.forEachIndexed { index, color ->
            // tworzy paint z kolorem
            val paint = android.graphics.Paint().apply {
                // ustawia kolor farby
                this.color = color.toColorInt()
                // ustawia styl wypelnienia
                style = android.graphics.Paint.Style.FILL
            }
            // rysuje prostokat dla kazdego koloru
            canvas.drawRect(
                (index * colorWidth).toFloat(),
                0f,
                ((index + 1) * colorWidth).toFloat(),
                height.toFloat(),
                paint
            )
        }

        // udostepnia utworzony bitmap
        shareBitmap(context, bitmap, "palette_${palette.name.replace(" ", "_")}.png")
    }

    /**
     * udostepnia bitmap jako plik obrazu
     */
    private fun shareBitmap(context: Context, bitmap: Bitmap, filename: String) {
        try {
            // tworzy sciezke do katalogu cache
            val cachePath = File(context.cacheDir, "images")
            // tworzy katalog jesli nie istnieje
            cachePath.mkdirs()
            // tworzy plik o podanej nazwie
            val file = File(cachePath, filename)
            
            // otwiera strumien wyjsciowy do pliku
            FileOutputStream(file).use { outputStream ->
                // kompresuje bitmap do formatu png i zapisuje
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            // tworzy uri za pomoca fileprovider
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // tworzy intent do udostepniania obrazu
            val intent = Intent(Intent.ACTION_SEND).apply {
                // ustawia typ jako png
                type = "image/png"
                // dodaje uri obrazu
                putExtra(Intent.EXTRA_STREAM, contentUri)
                // dodaje uprawnienia do odczytu
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // uruchamia chooser do wyboru aplikacji
            context.startActivity(Intent.createChooser(intent, "Share Image"))
        } catch (e: Exception) {
            // wypisuje stos bledu
            e.printStackTrace()
        }
    }
}
