package ru.artem_torpedo.diabetesdiary.ui.measurement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ru.artem_torpedo.diabetesdiary.R
import ru.artem_torpedo.diabetesdiary.data.local.entity.MeasurementEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import ru.artem_torpedo.diabetesdiary.ui.MainActivity
import ru.artem_torpedo.diabetesdiary.ui.foodlog.FoodLogActivity
import ru.artem_torpedo.diabetesdiary.ui.products.ProductsActivity
import ru.artem_torpedo.diabetesdiary.ui.reminders.RemindersActivity
import ru.artem_torpedo.diabetesdiary.ui.statistics.StatisticsActivity
import java.util.Calendar
import java.util.TimeZone


class MeasurementsActivity : AppCompatActivity() {

    private val viewModel: MeasurementViewModel by viewModels()

    private var fromDateMillis: Long? = null
    private var toDateMillis: Long? = null
    private lateinit var filterButton: Button

    private var measurementList: List<MeasurementEntity> = emptyList()

    private lateinit var adapter: ArrayAdapter<String>
    private val items = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurements)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1)
        val profileName = intent.getStringExtra(EXTRA_PROFILE_NAME)

        title = "Измерения: $profileName"

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_measurements
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_measurements -> true
                R.id.nav_statistics -> {
                    StatisticsActivity.start(
                        context = this,
                        profileId = profileId,
                        profileName = profileName ?: ""
                    )
                    true
                }

                R.id.nav_products -> {
                    ProductsActivity.start(this, profileId, profileName ?: "")
                    true
                }

                R.id.nav_food_log -> {
                    FoodLogActivity.start(this, profileId, profileName ?: "")
                    true
                }

                R.id.nav_reminders -> {
                    RemindersActivity.start(this, profileId, profileName ?: "")
                    true
                }

                else -> false
            }
        }


        filterButton = findViewById(R.id.filterButton)

        filterButton.setOnClickListener {
            if (fromDateMillis == null || toDateMillis == null) {
                showFilterDialog(profileId)
            } else {
                clearDateFilter(profileId)
            }
        }

        updateFilterButtonState()

        val listView: ListView = findViewById(R.id.measurementsList)
        val addButton: Button = findViewById(R.id.addMeasurementButton)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            items
        )
        listView.adapter = adapter

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val measurement = measurementList[position]
            showDeleteDialog(profileId, measurement)
            true
        }

        viewModel.measurements.observe(this) { measurements ->
            measurementList = measurements

            items.clear()
            items.addAll(
                measurements.map { m ->
                    buildString {
                        append(formatDate(m.dateTime))
                        append("\nСахар: ${m.glucoseLevel} ммоль/л")

                        m.insulinUnits?.let {
                            append("\nИнсулин: $it ЕД")
                        }

                        m.breadUnits?.let {
                            append("\nХЕ: $it")
                        }

                        m.comment?.let {
                            append("\nКомментарий: $it")
                        }
                    }
                }
            )
            adapter.notifyDataSetChanged()
        }

        viewModel.loadMeasurements(profileId)

        addButton.setOnClickListener {
            showAddDialog(profileId)
        }
    }

    private fun showAddDialog(profileId: Long) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_measurement, null)

        val glucoseInput = dialogView.findViewById<android.widget.EditText>(R.id.glucoseInput)
        val insulinInput = dialogView.findViewById<android.widget.EditText>(R.id.insulinInput)
        val breadInput = dialogView.findViewById<android.widget.EditText>(R.id.breadUnitsInput)
        val commentInput = dialogView.findViewById<android.widget.EditText>(R.id.commentInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Новое измерение")
            .setView(dialogView)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val glucoseRaw = glucoseInput.text.toString().replace(',', '.').trim()
                val glucose = glucoseRaw.toFloatOrNull()
                if (glucose == null) {
                    glucoseInput.error = "Введите число"
                    glucoseInput.requestFocus()
                    Toast.makeText(this, "Введите корректный сахар", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (glucose <= 0f || glucose > 40f) {
                    glucoseInput.error = "Диапазон 0–40"
                    glucoseInput.requestFocus()
                    Toast.makeText(this, "Сахар должен быть в диапазоне 0–40", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val insulin = run {
                    val raw = insulinInput.text.toString().replace(',', '.').trim()
                    if (raw.isBlank()) null else {
                        val v = raw.toFloatOrNull()
                        if (v == null) {
                            insulinInput.error = "Введите число"
                            insulinInput.requestFocus()
                            Toast.makeText(this, "Некорректный инсулин", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        if (v !in 0f..200f) {
                            insulinInput.error = "Диапазон 0–200"
                            insulinInput.requestFocus()
                            Toast.makeText(this, "Инсулин вне диапазона", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        v
                    }
                }

                val breadUnits = run {
                    val raw = breadInput.text.toString().replace(',', '.').trim()
                    if (raw.isBlank()) null else {
                        val v = raw.toFloatOrNull()
                        if (v == null) {
                            breadInput.error = "Введите число"
                            breadInput.requestFocus()
                            Toast.makeText(this, "Некорректные ХЕ", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        if (v !in 0f..50f) {
                            breadInput.error = "Диапазон 0–50"
                            breadInput.requestFocus()
                            Toast.makeText(this, "ХЕ вне диапазона", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        v
                    }
                }

                val comment = commentInput.text.toString().trim().takeIf { it.isNotBlank() }

                viewModel.addMeasurement(
                    profileId = profileId,
                    glucose = glucose,
                    insulin = insulin,
                    breadUnits = breadUnits,
                    comment = comment
                )

                dialog.dismiss()
            }
        }

        dialog.show()
    }


    private fun formatDate(timeMillis: Long): String {
        val formatter = java.text.SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            java.util.Locale.getDefault()
        )
        return formatter.format(java.util.Date(timeMillis))
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

            updateFilterButtonState()
            viewModel.loadMeasurementsByDate(profileId, fromDateMillis!!, toDateMillis!!)
        }

        picker.show(supportFragmentManager, "measurement_date_range_picker")
    }

    private fun clearDateFilter(profileId: Long) {
        fromDateMillis = null
        toDateMillis = null
        updateFilterButtonState()
        viewModel.loadMeasurements(profileId)
        Toast.makeText(this, "Фильтр сброшен", Toast.LENGTH_SHORT).show()
    }
    private fun updateFilterButtonState() {
        if (!::filterButton.isInitialized) return

        if (fromDateMillis == null || toDateMillis == null) {
            filterButton.text = "Фильтр по дате"
        } else {
            filterButton.text = "Сбросить фильтр"
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

    private fun showDeleteDialog(
        profileId: Long,
        measurement: MeasurementEntity
    ) {
        AlertDialog.Builder(this)
            .setTitle("Удалить измерение?")
            .setMessage("Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteMeasurement(profileId, measurement)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }


    companion object {
        private const val EXTRA_PROFILE_ID = "profile_id"
        private const val EXTRA_PROFILE_NAME = "profile_name"

        fun start(context: Context, profileId: Long, profileName: String) {
            val intent = Intent(context, MeasurementsActivity::class.java)
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