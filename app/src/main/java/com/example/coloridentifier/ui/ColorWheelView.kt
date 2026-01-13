package com.example.coloridentifier.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.coloridentifier.utils.ColorUtils
import kotlin.math.min

/**
 * Custom view for circular RGB color wheel
 */
class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var brightness = 1f
    
    private var selectedAngle = 0f
    private var onColorSelectedListener: ((Int) -> Unit)? = null

    init {
        indicatorPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 2f - 20f
        
        // Create color wheel gradient
        updateWheelGradient()
    }

    private fun updateWheelGradient() {
        // Create full spectrum of colors (360 degrees)
        val colors = IntArray(361)
        for (i in 0..360) {
            colors[i] = ColorUtils.getColorFromAngle(i.toFloat(), 1f, brightness)
        }
        
        val gradient = SweepGradient(centerX, centerY, colors, null)
        wheelPaint.shader = gradient
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw color wheel
        canvas.drawCircle(centerX, centerY, radius, wheelPaint)
        
        // Draw selection indicator
        val angle = Math.toRadians(selectedAngle.toDouble())
        val indicatorX = centerX + (radius * 0.85f * Math.cos(angle)).toFloat()
        val indicatorY = centerY + (radius * 0.85f * Math.sin(angle)).toFloat()
        
        canvas.drawCircle(indicatorX, indicatorY, 15f, indicatorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val touchX = event.x
                val touchY = event.y
                
                // Calculate angle
                val angle = ColorUtils.calculateAngle(touchX, touchY, centerX, centerY)
                selectedAngle = angle
                
                // Get color at that angle
                val selectedColor = ColorUtils.getColorFromAngle(angle, 1f, brightness)
                onColorSelectedListener?.invoke(selectedColor)
                
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Sets brightness value (0-1)
     */
    fun setBrightness(value: Float) {
        brightness = value.coerceIn(0f, 1f)
        updateWheelGradient()
        invalidate()
        
        // Update selected color with new brightness
        val selectedColor = ColorUtils.getColorFromAngle(selectedAngle, 1f, brightness)
        onColorSelectedListener?.invoke(selectedColor)
    }

    /**
     * Sets listener for color selection
     */
    fun setOnColorSelectedListener(listener: (Int) -> Unit) {
        onColorSelectedListener = listener
    }

    /**
     * Gets currently selected color
     */
    fun getSelectedColor(): Int {
        return ColorUtils.getColorFromAngle(selectedAngle, 1f, brightness)
    }
}
