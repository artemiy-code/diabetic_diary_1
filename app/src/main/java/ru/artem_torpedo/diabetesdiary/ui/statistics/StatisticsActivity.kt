package ru.artem_torpedo.diabetesdiary.ui.statistics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import ru.artem_torpedo.diabetesdiary.R
import ru.artem_torpedo.diabetesdiary.ui.MainActivity
import ru.artem_torpedo.diabetesdiary.ui.foodlog.FoodLogActivity
import ru.artem_torpedo.diabetesdiary.ui.measurement.MeasurementsActivity
import ru.artem_torpedo.diabetesdiary.ui.products.ProductsActivity
import ru.artem_torpedo.diabetesdiary.ui.reminders.RemindersActivity
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date

class StatisticsActivity : AppCompatActivity() {

    private val viewModel: StatisticsViewModel by viewModels()

    private var fromDateMillis: Long? = null
    private var isCustomFilterActive = false
    private var toDateMillis: Long? = null
    private lateinit var filterButton: Button
    private lateinit var periodText: TextView
    private lateinit var avgText: TextView
    private lateinit var minText: TextView
    private lateinit var maxText: TextView
    private lateinit var countText: TextView
    private lateinit var chart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1)
        val profileName = intent.getStringExtra(EXTRA_PROFILE_NAME).orEmpty()

        title = "Статистика: $profileName"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        periodText = findViewById(R.id.periodText)
        avgText = findViewById(R.id.avgValue)
        minText = findViewById(R.id.minValue)
        maxText = findViewById(R.id.maxValue)
        countText = findViewById(R.id.countValue)
        chart = findViewById(R.id.glucoseChart)
        setupChart()

        filterButton = findViewById(R.id.filterButtonStats)

        filterButton.setOnClickListener {
            if (isCustomFilterActive) {
                clearDateFilter(profileId)
            } else {
                showFilterDialog(profileId)
            }
        }

        updateFilterButtonState()

        updateFilterButtonState()

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_statistics
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_measurements -> {
                    MeasurementsActivity.start(this, profileId, profileName)
                    true
                }

                R.id.nav_statistics -> true
                R.id.nav_products -> {
                    ProductsActivity.start(this, profileId, profileName)
                    true
                }

                R.id.nav_food_log -> {
                    FoodLogActivity.start(this, profileId, profileName)
                    true
                }

                R.id.nav_reminders -> {
                    RemindersActivity.start(this, profileId, profileName)
                    true
                }

                else -> false
            }
        }

        viewModel.uiState.observe(this) { state ->
            periodText.text = state.periodLabel
            avgText.text = state.avgGlucose?.let { format1(it) } ?: "—"
            minText.text = state.minGlucose?.let { format1(it) } ?: "—"
            maxText.text = state.maxGlucose?.let { format1(it) } ?: "—"
            countText.text = state.count.toString()
            renderChart(state.points)
        }

        // по умолчанию стоит стата за последние 7 дней
        val now = System.currentTimeMillis()
        fromDateMillis = startOfDay(now - 7L * 24L * 60L * 60L * 1000L)
        toDateMillis = endOfDay(now)
        isCustomFilterActive = false

        load(profileId)
        updateFilterButtonState()
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.setNoDataText("Нет данных за период")

        chart.axisRight.isEnabled = false

        // отступы, чтобы подписи по Y не уезжали за экран
        chart.minOffset = 14f

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // убрал текстовые метки по оси X
        xAxis.setDrawLabels(false)
        xAxis.setDrawAxisLine(true)

        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawGridLines(true)
        leftAxis.granularity = 1f

        // чтобы значения по Y всегда помещались
        leftAxis.setDrawAxisLine(true)
        leftAxis.setDrawZeroLine(false)
    }

    private fun renderChart(points: List<ChartPoint>) {
        if (points.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }

        val sortedPoints = points.sortedBy { it.timeMillis }

        val entries = sortedPoints.mapIndexed { index, point ->
            Entry(index.toFloat(), point.glucose)
        }

        val dataSet = LineDataSet(entries, "Glucose").apply {
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircles(true)
            setDrawValues(false)
            mode = LineDataSet.Mode.LINEAR
            setDrawHorizontalHighlightIndicator(false)
        }

        val lineData = LineData(dataSet)
        chart.data = lineData

        // полностью убираем подписи времени по оси X
        chart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = ""
        }

        val minY = sortedPoints.minOf { it.glucose }
        val maxY = sortedPoints.maxOf { it.glucose }

        // делаем аккуратный диапазон оси Y с запасом
        val axisMin = (kotlin.math.floor(minY.toDouble()).toFloat() - 1f).coerceAtLeast(0f)
        val axisMax = kotlin.math.ceil(maxY.toDouble()).toFloat() + 1f

        chart.axisLeft.axisMinimum = axisMin
        chart.axisLeft.axisMaximum = axisMax

        // Если точек мало, показываем все, а если много, пользователь сможет скроллить
        chart.setVisibleXRangeMaximum(6f)
        chart.moveViewToX((entries.size - 1).coerceAtLeast(0).toFloat())

        chart.marker = GlucoseMarkerView(this, R.layout.view_glucose_marker, sortedPoints)

        chart.invalidate()
    }

    private fun load(profileId: Long) {
        val from = fromDateMillis ?: return
        val to = toDateMillis ?: return
        viewModel.loadStatistics(profileId, from, to)
    }

    private fun showFilterDialog(profileId: Long) {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Выберите период")
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val startUtc = selection.first
            val endUtc = selection.second

            if (startUtc == null || endUtc == null) {
                Toast.makeText(this, "Выберите обе даты", Toast.LENGTH_SHORT).show()
                return@addOnPositiveButtonClickListener
            }

            fromDateMillis = utcDateToLocalStartOfDay(startUtc)
            toDateMillis = utcDateToLocalEndOfDay(endUtc)
            isCustomFilterActive = true

            updateFilterButtonState()
            load(profileId)
        }

        picker.show(supportFragmentManager, "statistics_date_range_picker")
    }

    private fun clearDateFilter(profileId: Long) {
        val now = System.currentTimeMillis()
        fromDateMillis = startOfDay(now - 7L * 24L * 60L * 60L * 1000L)
        toDateMillis = endOfDay(now)
        isCustomFilterActive = false

        updateFilterButtonState()
        load(profileId)

        Toast.makeText(this, "Фильтр сброшен", Toast.LENGTH_SHORT).show()
    }

    private fun updateFilterButtonState() {
        if (!::filterButton.isInitialized) return

        if (isCustomFilterActive) {
            filterButton.text = "Сбросить фильтр"
        } else {
            filterButton.text = "Фильтр по дате"
        }
    }

    private fun utcDateToLocalStartOfDay(utcMillis: Long): Long {
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCalendar.timeInMillis = utcMillis

        val localCalendar = Calendar.getInstance()
        localCalendar.set(
            utcCalendar.get(Calendar.YEAR),
            utcCalendar.get(Calendar.MONTH),
            utcCalendar.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0
        )
        localCalendar.set(Calendar.MILLISECOND, 0)

        return localCalendar.timeInMillis
    }

    private fun utcDateToLocalEndOfDay(utcMillis: Long): Long {
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCalendar.timeInMillis = utcMillis

        val localCalendar = Calendar.getInstance()
        localCalendar.set(
            utcCalendar.get(Calendar.YEAR),
            utcCalendar.get(Calendar.MONTH),
            utcCalendar.get(Calendar.DAY_OF_MONTH),
            23,
            59,
            59
        )
        localCalendar.set(Calendar.MILLISECOND, 999)

        return localCalendar.timeInMillis
    }


    private fun startOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun endOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    private fun format1(v: Float): String = String.format(Locale.getDefault(), "%.1f", v)

    companion object {
        private const val EXTRA_PROFILE_ID = "profile_id"
        private const val EXTRA_PROFILE_NAME = "profile_name"

        fun start(context: Context, profileId: Long, profileName: String) {
            val intent = Intent(context, StatisticsActivity::class.java)
            intent.putExtra(EXTRA_PROFILE_ID, profileId)
            intent.putExtra(EXTRA_PROFILE_NAME, profileName)
            context.startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        goToProfiles()
        return true
    }

    private fun goToProfiles() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        finish()
    }

}