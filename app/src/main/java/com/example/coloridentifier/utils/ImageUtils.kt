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
 * Utility object for image processing operations
 */
object ImageUtils {

    /**
     * Loads bitmap from URI with proper orientation
     */
    fun loadBitmapFromUri(context: Context, uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap? {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            val inputStream2: InputStream? = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
            inputStream2?.close()

            // Fix orientation
            bitmap = bitmap?.let { fixOrientation(context, uri, it) }

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Calculates sample size for bitmap decoding
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Fixes image orientation based on EXIF data
     */
    private fun fixOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()

            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(bitmap, horizontal = true, vertical = false)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(bitmap, horizontal = false, vertical = true)
                else -> bitmap
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return bitmap
        }
    }

    /**
     * Rotates bitmap by specified degrees
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Flips bitmap horizontally or vertically
     */
    private fun flipBitmap(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(
            if (horizontal) -1f else 1f,
            if (vertical) -1f else 1f
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Scales bitmap to specified dimensions
     */
    fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val scale = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Creates a bitmap with single color
     */
    fun createColorBitmap(color: Int, width: Int = 100, height: Int = 100): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return bitmap
    }
}
