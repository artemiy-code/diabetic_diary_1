package ru.artem_torpedo.diabetesdiary.ui.statistics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

data class ChartPoint(val timeMillis: Long, val glucose: Float)

class GlucoseLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 3f
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 42f
    }

    private var points: List<ChartPoint> = emptyList()

    fun setMeasurements(newPoints: List<ChartPoint>) {
        points = newPoints.sortedBy { it.timeMillis }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val leftPad = 80f
        val topPad = 30f
        val rightPad = 30f
        val bottomPad = 60f

        val plotLeft = leftPad
        val plotTop = topPad
        val plotRight = w - rightPad
        val plotBottom = h - bottomPad

        canvas.drawLine(plotLeft, plotTop, plotLeft, plotBottom, axisPaint)
        canvas.drawLine(plotLeft, plotBottom, plotRight, plotBottom, axisPaint)

        if (points.isEmpty()) {
            canvas.drawText("    Нет данных за период", plotLeft, plotTop + 50f, textPaint)
            return
        }

        val minY = points.minOf { it.glucose }
        val maxY = points.maxOf { it.glucose }
        val ySpan = max(0.1f, maxY - minY)

        val minX = points.minOf { it.timeMillis }
        val maxX = points.maxOf { it.timeMillis }
        val xSpan = max(1L, maxX - minX).toFloat()

        canvas.drawText("max: ${format1(maxY)}", plotLeft + 40f, plotTop + 20f, textPaint)
        canvas.drawText("min: ${format1(minY)}", plotLeft + 40f, plotBottom + 40f, textPaint)

        var prevX: Float? = null
        var prevY: Float? = null

        for (p in points) {
            val x = plotLeft + ((p.timeMillis - minX).toFloat() / xSpan) * (plotRight - plotLeft)
            val y = plotBottom - ((p.glucose - minY) / ySpan) * (plotBottom - plotTop)

            if (prevX != null && prevY != null) {
                canvas.drawLine(prevX, prevY, x, y, linePaint)
            }
            prevX = x
            prevY = y
        }
    }

    private fun format1(v: Float): String = String.format("%.1f", v)
}