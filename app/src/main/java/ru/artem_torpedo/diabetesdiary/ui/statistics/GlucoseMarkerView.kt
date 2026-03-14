package ru.artem_torpedo.diabetesdiary.ui.statistics

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import ru.artem_torpedo.diabetesdiary.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlucoseMarkerView(
    context: Context,
    layoutResource: Int,
    private val points: List<ChartPoint>
) : MarkerView(context, layoutResource) {

    private val dateText: TextView = findViewById(R.id.markerDateText)
    private val valueText: TextView = findViewById(R.id.markerValueText)

    private val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            val index = e.x.toInt()
            if (index in points.indices) {
                val point = points[index]
                dateText.text = formatter.format(Date(point.timeMillis))
                valueText.text = "Сахар: ${String.format(Locale.getDefault(), "%.1f", point.glucose)}"
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset() = com.github.mikephil.charting.utils.MPPointF(
        -(width / 2f),
        -height.toFloat()
    )
}