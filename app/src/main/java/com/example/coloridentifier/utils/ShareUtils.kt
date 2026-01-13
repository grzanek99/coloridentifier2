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
 * Utility object for sharing and clipboard operations
 */
object ShareUtils {

    /**
     * Copies text to clipboard
     */
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * Shares color as text
     */
    fun shareColorAsText(context: Context, color: Color) {
        val shareText = """
            Color: ${color.name}
            HEX: ${color.hexValue}
            RGB: ${color.getRGBString()}
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Color: ${color.name}")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Color"))
    }

    /**
     * Shares color as image
     */
    fun shareColorAsImage(context: Context, color: Color) {
        val bitmap = ImageUtils.createColorBitmap(color.toColorInt(), 500, 500)
        shareBitmap(context, bitmap, "color_${color.name.replace(" ", "_")}.png")
    }

    /**
     * Shares palette as text
     */
    fun sharePaletteAsText(context: Context, palette: Palette) {
        val shareText = palette.toTextString()
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Palette: ${palette.name}")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Palette"))
    }

    /**
     * Shares palette as JSON
     */
    fun sharePaletteAsJson(context: Context, palette: Palette) {
        val jsonText = palette.toJsonString()
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "Palette: ${palette.name}")
            putExtra(Intent.EXTRA_TEXT, jsonText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Palette JSON"))
    }

    /**
     * Shares palette as image (horizontal visualization of colors)
     */
    fun sharePaletteAsImage(context: Context, palette: Palette) {
        val colorCount = palette.colors.size
        if (colorCount == 0) return

        // Create bitmap with all colors side by side
        val colorWidth = 200
        val totalWidth = colorWidth * colorCount
        val height = 500

        val bitmap = Bitmap.createBitmap(totalWidth, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        palette.colors.forEachIndexed { index, color ->
            val paint = android.graphics.Paint().apply {
                this.color = color.toColorInt()
                style = android.graphics.Paint.Style.FILL
            }
            canvas.drawRect(
                (index * colorWidth).toFloat(),
                0f,
                ((index + 1) * colorWidth).toFloat(),
                height.toFloat(),
                paint
            )
        }

        shareBitmap(context, bitmap, "palette_${palette.name.replace(" ", "_")}.png")
    }

    /**
     * Shares a bitmap as image file
     */
    private fun shareBitmap(context: Context, bitmap: Bitmap, filename: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, filename)
            
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Image"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
