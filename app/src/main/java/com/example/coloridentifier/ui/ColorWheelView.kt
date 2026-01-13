package com.example.coloridentifier.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.coloridentifier.utils.ColorUtils
import kotlin.math.min

/**
 * Custom view for circular RGB color wheel with saturation gradient
 */
class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var value = 1f  // Zmienione z brightness na value (dla HSV)
    
    private var selectedAngle = 0f
    private var selectedSaturation = 1f  // Nowe pole dla saturacji
    private var onColorSelectedListener: ((Int) -> Unit)? = null

    init {
        indicatorPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        indicatorFillPaint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 2f - 20f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Rysuj koło koloru z gradientem saturacji (od białego w środku do pełnego koloru na krawędzi)
        // Używamy podejścia z wieloma małymi segmentami dla płynnego gradientu
        val angleStep = 2f  // Co 2 stopnie dla wydajności
        val radiusSteps = 30  // 30 pierścieni radialnych
        
        for (angleIndex in 0 until (360 / angleStep.toInt())) {
            val angle = angleIndex * angleStep
            
            for (radiusIndex in 0 until radiusSteps) {
                val saturation = (radiusIndex + 1).toFloat() / radiusSteps
                val currentRadius = radius * radiusIndex / radiusSteps
                val nextRadius = radius * (radiusIndex + 1) / radiusSteps
                
                val color = ColorUtils.getColorFromAngle(angle, saturation, value)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.color = color
                paint.style = Paint.Style.FILL
                
                val rect = android.graphics.RectF(
                    centerX - nextRadius,
                    centerY - nextRadius,
                    centerX + nextRadius,
                    centerY + nextRadius
                )
                canvas.drawArc(rect, angle - angleStep/2, angleStep, true, paint)
            }
        }
        
        // Rysuj wskaźnik wyboru
        val distance = radius * selectedSaturation
        val angle = Math.toRadians(selectedAngle.toDouble())
        val indicatorX = centerX + (distance * Math.cos(angle)).toFloat()
        val indicatorY = centerY + (distance * Math.sin(angle)).toFloat()
        
        // Rysuj białe kółko z czarnym obramowaniem
        indicatorPaint.color = Color.BLACK
        indicatorPaint.strokeWidth = 3f
        canvas.drawCircle(indicatorX, indicatorY, 12f, indicatorPaint)
        indicatorFillPaint.color = Color.WHITE
        canvas.drawCircle(indicatorX, indicatorY, 10f, indicatorFillPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val touchX = event.x
                val touchY = event.y
                
                // Oblicz kąt
                val angle = ColorUtils.calculateAngle(touchX, touchY, centerX, centerY)
                selectedAngle = angle
                
                // Oblicz odległość od środka
                val distance = ColorUtils.calculateDistance(touchX, touchY, centerX, centerY)
                // Normalizuj saturację (0 = środek/biały, 1 = krawędź/pełny kolor)
                selectedSaturation = (distance / radius).coerceIn(0f, 1f)
                
                // Pobierz kolor z kąta i saturacji
                val selectedColor = ColorUtils.getColorFromAngle(angle, selectedSaturation, value)
                onColorSelectedListener?.invoke(selectedColor)
                
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Ustawia wartość value (0-1) - od czarnego do pełnego koloru
     */
    fun setValue(newValue: Float) {
        value = newValue.coerceIn(0f, 1f)
        invalidate()
        
        // Aktualizuj wybrany kolor z nową wartością
        val selectedColor = ColorUtils.getColorFromAngle(selectedAngle, selectedSaturation, value)
        onColorSelectedListener?.invoke(selectedColor)
    }

    /**
     * Zachowana metoda dla kompatybilności wstecznej
     */
    fun setBrightness(brightness: Float) {
        setValue(brightness)
    }

    /**
     * Ustawia listener dla wyboru koloru
     */
    fun setOnColorSelectedListener(listener: (Int) -> Unit) {
        onColorSelectedListener = listener
    }

    /**
     * Pobiera aktualnie wybrany kolor
     */
    fun getSelectedColor(): Int {
        return ColorUtils.getColorFromAngle(selectedAngle, selectedSaturation, value)
    }
    
    /**
     * Pobiera aktualny hue (dla gradientu suwaka)
     */
    fun getSelectedHue(): Float {
        return selectedAngle
    }
}
