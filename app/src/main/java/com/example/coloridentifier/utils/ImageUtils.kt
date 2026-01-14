package com.example.coloridentifier.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.io.InputStream

/**
 * obiekt narzedzowy do operacji na obrazach
 */
object ImageUtils {

    /**
     * laduje bitmap z uri z poprawna orientacja
     */
    fun loadBitmapFromUri(context: Context, uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap? {
        try {
            // otwiera strumien wejsciowy z uri
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            
            // tworzy opcje dekodowania obrazu
            val options = BitmapFactory.Options()
            // ustawia tylko sprawdzanie wymiarow bez ladowania pikseli
            options.inJustDecodeBounds = true
            // dekoduje strumien zeby uzyskac wymiary
            BitmapFactory.decodeStream(inputStream, null, options)
            // zamyka strumien
            inputStream?.close()

            // oblicza wspolczynnik probkowania dla zmniejszenia rozmiaru
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

            // wylacza tryb tylko wymiarow
            options.inJustDecodeBounds = false
            // otwiera ponownie strumien
            val inputStream2: InputStream? = context.contentResolver.openInputStream(uri)
            // dekoduje obraz z obliczonym wspolczynnikiem probkowania
            var bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
            // zamyka strumien
            inputStream2?.close()

            // naprawia orientacje na podstawie exif
            bitmap = bitmap?.let { fixOrientation(context, uri, it) }

            // zwraca zaladowany bitmap
            return bitmap
        } catch (e: Exception) {
            // wypisuje stos bledu
            e.printStackTrace()
            // zwraca null w przypadku bledu
            return null
        }
    }

    /**
     * oblicza wspolczynnik probkowania dla dekodowania bitmap
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // pobiera wysokosc oryginalnego obrazu
        val height = options.outHeight
        // pobiera szerokosc oryginalnego obrazu
        val width = options.outWidth
        // inicjalizuje wspolczynnik probkowania jako 1
        var inSampleSize = 1

        // sprawdza czy obraz przekracza wymagane wymiary
        if (height > reqHeight || width > reqWidth) {
            // oblicza polowe wysokosci
            val halfHeight = height / 2
            // oblicza polowe szerokosci
            val halfWidth = width / 2

            // zwielokrotnia wspolczynnik az obraz zmiesci sie w wymaganych wymiarach
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                // podwaja wspolczynnik probkowania
                inSampleSize *= 2
            }
        }

        // zwraca obliczony wspolczynnik
        return inSampleSize
    }

    /**
     * naprawia orientacje obrazu na podstawie danych exif
     */
    private fun fixOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        try {
            // otwiera strumien do odczytu exif
            val inputStream = context.contentResolver.openInputStream(uri)
            // tworzy obiekt exif z strumienia
            val exif = inputStream?.let { ExifInterface(it) }
            // zamyka strumien
            inputStream?.close()

            // pobiera orientacje z danych exif
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            // wykonuje transformacje na podstawie orientacji
            return when (orientation) {
                // obraca o 90 stopni w prawo
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                // obraca o 180 stopni
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                // obraca o 270 stopni w prawo
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                // odbija w poziomie
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(bitmap, horizontal = true, vertical = false)
                // odbija w pionie
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(bitmap, horizontal = false, vertical = true)
                // zwraca bez zmian dla normalnej orientacji
                else -> bitmap
            }
        } catch (e: IOException) {
            // wypisuje stos bledu
            e.printStackTrace()
            // zwraca oryginalny bitmap
            return bitmap
        }
    }

    /**
     * obraca bitmap o podana liczbe stopni
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        // tworzy macierz transformacji
        val matrix = Matrix()
        // ustawia rotacje
        matrix.postRotate(degrees)
        // tworzy nowy bitmap z transformacja
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * odbija bitmap w poziomie lub pionie
     */
    private fun flipBitmap(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        // tworzy macierz transformacji
        val matrix = Matrix()
        // ustawia skalowanie dla odbicia
        matrix.preScale(
            if (horizontal) -1f else 1f,
            if (vertical) -1f else 1f
        )
        // tworzy nowy bitmap z transformacja
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * skaluje bitmap do okreslonych wymiarow
     */
    fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        // pobiera szerokosc bitmap
        val width = bitmap.width
        // pobiera wysokosc bitmap
        val height = bitmap.height

        // sprawdza czy obraz juz miesci sie w wymaganych wymiarach
        if (width <= maxWidth && height <= maxHeight) {
            // zwraca oryginalny bitmap
            return bitmap
        }

        // oblicza skale zachowujac proporcje
        val scale = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        // oblicza nowa szerokosc
        val newWidth = (width * scale).toInt()
        // oblicza nowa wysokosc
        val newHeight = (height * scale).toInt()

        // tworzy i zwraca przeskalowany bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * tworzy bitmap z jednym kolorem
     */
    fun createColorBitmap(color: Int, width: Int = 100, height: Int = 100): Bitmap {
        // tworzy nowy bitmap o podanych wymiarach
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // wypelnia caly bitmap jednym kolorem
        bitmap.eraseColor(color)
        // zwraca utworzony bitmap
        return bitmap
    }
}
