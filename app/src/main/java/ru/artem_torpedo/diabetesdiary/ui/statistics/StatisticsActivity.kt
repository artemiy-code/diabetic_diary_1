package ru.artem_torpedo.diabetesdiary.ui.statistics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.artem_torpedo.diabetesdiary.R
import ru.artem_torpedo.diabetesdiary.ui.MainActivity
import ru.artem_torpedo.diabetesdiary.ui.foodlog.FoodLogActivity
import ru.artem_torpedo.diabetesdiary.ui.products.ProductsActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private val viewModel: StatisticsViewModel by viewModels()

    private var fromDateMillis: Long? = null
    private var toDateMillis: Long? = null

    private lateinit var periodText: TextView
    private lateinit var avgText: TextView
    private lateinit var minText: TextView
    private lateinit var maxText: TextView
    private lateinit var countText: TextView
    private lateinit var chart: GlucoseLineChartView

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

        val filterButton: Button = findViewById(R.id.filterButtonStats)
        filterButton.setOnClickListener { showFilterDialog(profileId) }

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_statistics
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_measurements -> {
                    finish()
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
                else -> false
            }
        }

        viewModel.uiState.observe(this) { state ->
            periodText.text = state.periodLabel
            avgText.text = state.avgGlucose?.let { format1(it) } ?: "—"
            minText.text = state.minGlucose?.let { format1(it) } ?: "—"
            maxText.text = state.maxGlucose?.let { format1(it) } ?: "—"
            countText.text = state.count.toString()
            chart.setMeasurements(state.points)
        }

        // по умолчанию стоит стата за последние 7 дней
        val now = System.currentTimeMillis()
        fromDateMillis = startOfDay(now - 7L * 24L * 60L * 60L * 1000L)
        toDateMillis = endOfDay(now)

        load(profileId)
    }

    private fun load(profileId: Long) {
        val from = fromDateMillis ?: return
        val to = toDateMillis ?: return
        viewModel.loadStatistics(profileId, from, to)
    }

    private fun showFilterDialog(profileId: Long) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_date_filter, null)

        val fromButton = dialogView.findViewById<Button>(R.id.fromDateButton)
        val toButton = dialogView.findViewById<Button>(R.id.toDateButton)

        fromDateMillis?.let { fromButton.text = "С даты: ${formatOnlyDate(it)}" }
        toDateMillis?.let { toButton.text = "По дату: ${formatOnlyDate(it)}" }

        fromButton.setOnClickListener {
            showDatePicker { selectedDate ->
                fromDateMillis = startOfDay(selectedDate)
                fromButton.text = "С даты: ${formatOnlyDate(selectedDate)}"
            }
        }

        toButton.setOnClickListener {
            showDatePicker { selectedDate ->
                toDateMillis = endOfDay(selectedDate)
                toButton.text = "По дату: ${formatOnlyDate(selectedDate)}"
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Период статистики")
            .setView(dialogView)
            .setPositiveButton("Применить") { _, _ -> load(profileId) }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()

        val dialog = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0)
                selectedCalendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(selectedCalendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.show()
    }

    private fun formatOnlyDate(timeMillis: Long): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return formatter.format(Date(timeMillis))
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
