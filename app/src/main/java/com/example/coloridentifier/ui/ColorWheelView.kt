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
 * niestandardowy widok przedstawiajacy kolo kolorow rgb
 * umozliwia wybor kolorow przez dotyk
 */
class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // paint do rysowania kola kolorow
    private val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    // paint do rysowania obramowania wskaznika
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    // paint do rysowania wypelnienia wskaznika
    private val indicatorFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // pozycja kursora na osi x
    private var cursorX = 0f
    // pozycja kursora na osi y
    private var cursorY = 0f
    // flaga okreslajaca czy kursor jest widoczny
    private var showCursor = false
    
    // promien kursora w pikselach
    private val cursorRadius = 15f
    // szerokosc obramowania kursora
    private val cursorStrokeWidth = 3f
    // paint do rysowania wypelnienia kursora
    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    // paint do rysowania obramowania kursora
    private val cursorStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // ustawia styl na obramowanie bez wypelnienia
        style = Paint.Style.STROKE
        // ustawia szerokosc linii
        strokeWidth = cursorStrokeWidth
        // ustawia kolor na czarny
        color = Color.BLACK
    }
    
    // wspolrzedna x srodka kola
    private var centerX = 0f
    // wspolrzedna y srodka kola
    private var centerY = 0f
    // promien kola kolorow
    private var radius = 0f
    // wartosc jasnosci w modelu hsv od 0 do 1
    private var value = 1f
    
    // aktualnie wybrany kat w stopniach od 0 do 360
    private var selectedAngle = 0f
    // aktualna saturacja od 0 (srodek) do 1 (krawedz)
    private var selectedSaturation = 1f
    // callback wywoływany gdy wybrano nowy kolor
    private var onColorSelectedListener: ((Int) -> Unit)? = null

    init {
        // konfiguruje paint obramowania wskaznika
        indicatorPaint.apply {
            // ustawia kolor na bialy
            color = Color.WHITE
            // ustawia styl na obramowanie
            style = Paint.Style.STROKE
            // ustawia szerokosc linii
            strokeWidth = 8f
        }
        // konfiguruje paint wypelnienia wskaznika
        indicatorFillPaint.apply {
            // ustawia kolor na bialy
            color = Color.WHITE
            // ustawia styl na wypelniony
            style = Paint.Style.FILL
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // oblicza wspolrzedna x srodka
        centerX = w / 2f
        // oblicza wspolrzedna y srodka
        centerY = h / 2f
        // oblicza promien jako mniejszy wymiar minus margines
        radius = min(w, h) / 2f - 20f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // rysuje kolo koloru z gradientem saturacji
        // od bialego w srodku do pelnego koloru na krawedzi
        // krok katu co 2 stopnie dla wydajnosci
        val angleStep = 2f
        // liczba pierscieni radialnych dla gradientu
        val radiusSteps = 30
        
        // iteruje przez wszystkie katy kola
        for (angleIndex in 0 until (360 / angleStep.toInt())) {
            // oblicza aktualny kat w stopniach
            val angle = angleIndex * angleStep
            
            // iteruje przez pierscienie radialne
            for (radiusIndex in 0 until radiusSteps) {
                // oblicza saturacje dla tego pierscienia
                val saturation = (radiusIndex + 1).toFloat() / radiusSteps
                // oblicza promien wewnetrzny pierscienia
                val currentRadius = radius * radiusIndex / radiusSteps
                // oblicza promien zewnetrzny pierscienia
                val nextRadius = radius * (radiusIndex + 1) / radiusSteps
                
                // pobiera kolor dla kata saturacji i wartosci
                val color = ColorUtils.getColorFromAngle(angle, saturation, value)
                // tworzy paint z antialiasing
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                // ustawia kolor farby
                paint.color = color
                // ustawia styl na wypelniony
                paint.style = Paint.Style.FILL
                
                // tworzy prostokat ograniczajacy luk
                val rect = android.graphics.RectF(
                    centerX - nextRadius,
                    centerY - nextRadius,
                    centerX + nextRadius,
                    centerY + nextRadius
                )
                // rysuje luk kola w danym kacie
                canvas.drawArc(rect, angle - angleStep/2, angleStep, true, paint)
            }
        }
        
        // oblicza odleglosc wskaznika od srodka
        val distance = radius * selectedSaturation
        // konwertuje kat na radiany
        val angle = Math.toRadians(selectedAngle.toDouble())
        // oblicza wspolrzedna x wskaznika
        val indicatorX = centerX + (distance * Math.cos(angle)).toFloat()
        // oblicza wspolrzedna y wskaznika
        val indicatorY = centerY + (distance * Math.sin(angle)).toFloat()
        
        // rysuje czarne obramowanie wskaznika
        indicatorPaint.color = Color.BLACK
        // ustawia szerokosc obramowania
        indicatorPaint.strokeWidth = 3f
        // rysuje zewnetrzny okrag obramowania
        canvas.drawCircle(indicatorX, indicatorY, 12f, indicatorPaint)
        // ustawia kolor wypelnienia na bialy
        indicatorFillPaint.color = Color.WHITE
        // rysuje bialy okrag wewnatrz
        canvas.drawCircle(indicatorX, indicatorY, 10f, indicatorFillPaint)
        
        // rysuje kursor jesli jest widoczny
        if (showCursor) {
            drawCursor(canvas)
        }
    }

    /**
     * rysuje kursor w aktualnej pozycji
     */
    private fun drawCursor(canvas: Canvas) {
        // ustawia kolor wypelnienia na bialy
        cursorPaint.color = Color.WHITE
        // ustawia styl na wypelniony
        cursorPaint.style = Paint.Style.FILL
        // rysuje bialy okrag wypelnienia
        canvas.drawCircle(cursorX, cursorY, cursorRadius, cursorPaint)
        
        // rysuje czarne obramowanie kursora
        canvas.drawCircle(cursorX, cursorY, cursorRadius, cursorStrokePaint)
        
        // ustawia kolor na czarny dla srodkowego punktu
        cursorPaint.color = Color.BLACK
        // rysuje maly czarny punkt w centrum kursora
        canvas.drawCircle(cursorX, cursorY, 3f, cursorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // sprawdza typ eventu dotyku
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // pobiera wspolrzedna x dotyku
                val touchX = event.x
                // pobiera wspolrzedna y dotyku
                val touchY = event.y
                
                // oblicza odleglosc dotyku od srodka kola
                val distance = ColorUtils.calculateDistance(touchX, touchY, centerX, centerY)
                // sprawdza czy dotyk jest wewnatrz kola kolorow
                if (distance <= radius) {
                    // aktualizuje pozycje kursora na osi x
                    cursorX = touchX
                    // aktualizuje pozycje kursora na osi y
                    cursorY = touchY
                    // pokazuje kursor
                    showCursor = true
                    
                    // oblicza kat od srodka kola
                    val angle = ColorUtils.calculateAngle(touchX, touchY, centerX, centerY)
                    // zapisuje wybrany kat
                    selectedAngle = angle
                    
                    // normalizuje saturacje do zakresu 0-1
                    // 0 to srodek bialy 1 to krawedz pelny kolor
                    selectedSaturation = (distance / radius).coerceIn(0f, 1f)
                    
                    // pobiera kolor z kata saturacji i wartosci
                    val selectedColor = ColorUtils.getColorFromAngle(angle, selectedSaturation, value)
                    // wywoluje callback z wybranym kolorem
                    onColorSelectedListener?.invoke(selectedColor)
                    
                    // odswieza widok zeby pokazac zmiany
                    invalidate()
                    // zwraca true event zostal obsluzony
                    return true
                } else {
                    // ukrywa kursor gdy dotyk poza kolem
                    showCursor = false
                    // odswieza widok
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // ukrywa kursor po puszczeniu palca
                showCursor = false
                // odswieza widok
                invalidate()
            }
        }
        // przekazuje event do domyslnej implementacji
        return super.onTouchEvent(event)
    }

    /**
     * ustawia wartosc value w modelu hsv od 0 do 1
     * 0 to czarny 1 to pelny kolor
     */
    fun setValue(newValue: Float) {
        // ogranicza wartosc do zakresu 0-1
        value = newValue.coerceIn(0f, 1f)
        // odswieza widok
        invalidate()
        
        // pobiera wybrany kolor z nowa wartoscia
        val selectedColor = ColorUtils.getColorFromAngle(selectedAngle, selectedSaturation, value)
        // wywoluje callback z nowym kolorem
        onColorSelectedListener?.invoke(selectedColor)
    }

    /**
     * ustawia jasnosc kompatybilnosc wsteczna
     */
    fun setBrightness(brightness: Float) {
        // deleguje do setvalue
        setValue(brightness)
    }

    /**
     * ustawia listener wywoływany gdy wybrano kolor
     */
    fun setOnColorSelectedListener(listener: (Int) -> Unit) {
        // przypisuje listener
        onColorSelectedListener = listener
    }

    /**
     * pobiera aktualnie wybrany kolor jako int
     */
    fun getSelectedColor(): Int {
        // zwraca kolor z aktualnych parametrow
        return ColorUtils.getColorFromAngle(selectedAngle, selectedSaturation, value)
    }
    
    /**
     * pobiera aktualny hue w stopniach dla gradientu suwaka
     */
    fun getSelectedHue(): Float {
        // zwraca wybrany kat
        return selectedAngle
    }
}
