package ru.artem_torpedo.diabetesdiary.ui.foodlog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import ru.artem_torpedo.diabetesdiary.R
import ru.artem_torpedo.diabetesdiary.data.local.entity.FoodEntryWithProduct
import ru.artem_torpedo.diabetesdiary.ui.MainActivity
import ru.artem_torpedo.diabetesdiary.ui.measurement.MeasurementsActivity
import ru.artem_torpedo.diabetesdiary.ui.products.ProductsActivity
import ru.artem_torpedo.diabetesdiary.ui.reminders.RemindersActivity
import ru.artem_torpedo.diabetesdiary.ui.statistics.StatisticsActivity
import java.util.Calendar
import java.util.TimeZone

class FoodLogActivity : AppCompatActivity() {

    private val viewModel: FoodLogViewModel by viewModels()

    private var fromDateMillis: Long? = null
    private var toDateMillis: Long? = null
    private lateinit var filterButton: Button

    private var foodList: List<FoodEntryWithProduct> = emptyList()
    private lateinit var adapter: ArrayAdapter<String>
    private val items = mutableListOf<String>()

    private lateinit var totalCaloriesText: TextView

    private var profileId: Long = -1
    private var profileName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_log)

        profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1)
        profileName = intent.getStringExtra(EXTRA_PROFILE_NAME).orEmpty()
        title = "Питание: $profileName"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        totalCaloriesText = findViewById(R.id.totalCaloriesText)

        val listView: ListView = findViewById(R.id.foodLogList)
        val addButton: Button = findViewById(R.id.addFoodEntryButton)

        filterButton = findViewById(R.id.filterFoodButton)

        filterButton.setOnClickListener {
            if (fromDateMillis == null || toDateMillis == null) {
                showFilterDialog()
            } else {
                clearDateFilter()
            }
        }

        updateFilterButtonState()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val entry = foodList[position]
            showDeleteDialog(entry.entryId)
            true
        }

        addButton.setOnClickListener {
            showAddFoodDialog()
        }

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_food_log
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_measurements -> {
                    MeasurementsActivity.start(this, profileId, profileName)
                    true
                }

                R.id.nav_statistics -> {
                    StatisticsActivity.start(this, profileId, profileName)
                    true
                }

                R.id.nav_products -> {
                    ProductsActivity.start(this, profileId, profileName)
                    true
                }

                R.id.nav_food_log -> true
                R.id.nav_reminders -> {
                    RemindersActivity.start(this, profileId, profileName)
                    true
                }

                else -> false
            }
        }

        viewModel.products.observe(this) { /* продукты доступны для диалога */ }

        viewModel.foodLog.observe(this) { list ->
            foodList = list

            items.clear()
            items.addAll(list.map { e ->
                val kcal = e.grams * e.caloriesPer100g / 100f
                val carbs = e.grams * e.carbsPer100g / 100f
                val prot = e.grams * e.proteinPer100g / 100f
                val fat = e.grams * e.fatPer100g / 100f

                buildString {
                    append(formatDate(e.dateTime))
                    append("\n${e.productName}, ${fmt1(e.grams)} г")
                    append(
                        "\nКкал: ${fmt1(kcal)}, Б: ${fmt1(prot)}, Ж: ${fmt1(fat)}, У: ${
                            fmt1(
                                carbs
                            )
                        }"
                    )
                    e.comment?.takeIf { it.isNotBlank() }?.let { append("\nКомментарий: $it") }
                }
            })
            adapter.notifyDataSetChanged()

            val total = list.sumOf { (it.grams * it.caloriesPer100g / 100f).toDouble() }.toFloat()
            totalCaloriesText.text = "Калории за период: ${fmt1(total)}"
        }

        viewModel.loadProducts()
        viewModel.loadFoodLog(profileId)
    }

    private fun showAddFoodDialog() {
        val allProducts = viewModel.products.value.orEmpty()
        if (allProducts.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Нет продуктов")
                .setMessage("Сначала добавь продукты в базе.")
                .setPositiveButton("Ок", null)
                .show()
            return
        }

        showProductPickerDialog(allProducts) { selectedProduct ->
            showGramsDialog(selectedProduct.id, selectedProduct.name)
        }
    }

    private fun showProductPickerDialog(
        allProducts: List<ru.artem_torpedo.diabetesdiary.data.local.entity.ProductEntity>,
        onPicked: (ru.artem_torpedo.diabetesdiary.data.local.entity.ProductEntity) -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pick_product, null)
        val searchInput = dialogView.findViewById<EditText>(R.id.searchInput)
        val listView = dialogView.findViewById<ListView>(R.id.productsList)

        // Стартовый список сделал первые 50. Уменьшить, если будет лагать
        var filtered = allProducts
        val display = mutableListOf<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, display)
        listView.adapter = adapter

        fun rebuildList(query: String) {
            val q = query.trim().lowercase()
            filtered = if (q.isEmpty()) {
                allProducts.take(50)
            } else {
                allProducts.asSequence()
                    .filter { it.name.lowercase().contains(q) }
                    .take(100)
                    .toList()
            }

            display.clear()
            display.addAll(filtered.map { it.name })
            adapter.notifyDataSetChanged()
        }

        rebuildList("")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Выбор продукта")
            .setView(dialogView)
            .setNegativeButton("Отмена", null)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            val picked = filtered[position]
            dialog.dismiss()
            onPicked(picked)
        }

        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                rebuildList(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        dialog.show()
    }

    private fun showGramsDialog(productId: Long, productName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_food_grams, null)
        val selectedText = dialogView.findViewById<TextView>(R.id.selectedProductText)
        val gramsInput = dialogView.findViewById<EditText>(R.id.gramsInput)
        val commentInput = dialogView.findViewById<EditText>(R.id.commentInput)

        selectedText.text = "Продукт: $productName"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Добавить прием пищи")
            .setView(dialogView)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val raw = gramsInput.text.toString().replace(',', '.').trim()
                val grams = raw.toFloatOrNull()

                if (grams == null) {
                    gramsInput.error = "Введите число"
                    gramsInput.requestFocus()
                    return@setOnClickListener
                }
                if (grams > 2500f) {
                    gramsInput.error = "Слишком большое значение"
                    gramsInput.requestFocus()
                    return@setOnClickListener
                }

                val comment = commentInput.text.toString().trim().takeIf { it.isNotBlank() }
                viewModel.addEntry(profileId, productId, grams, comment)
                dialog.dismiss()
            }
        }

        dialog.show()
    }


    private fun showFilterDialog() {
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
            viewModel.loadFoodLogByDate(profileId, fromDateMillis!!, toDateMillis!!)
        }

        picker.show(supportFragmentManager, "foodlog_date_range_picker")
    }

    private fun clearDateFilter() {
        fromDateMillis = null
        toDateMillis = null

        updateFilterButtonState()
        viewModel.loadFoodLog(profileId)

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

    private fun showDeleteDialog(entryId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Удалить запись?")
            .setMessage("Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteEntry(profileId, entryId)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun formatDate(timeMillis: Long): String {
        val formatter =
            java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        return formatter.format(java.util.Date(timeMillis))
    }

    private fun fmt1(v: Float): String = String.format(java.util.Locale.getDefault(), "%.1f", v)

    companion object {
        private const val EXTRA_PROFILE_ID = "profile_id"
        private const val EXTRA_PROFILE_NAME = "profile_name"

        fun start(context: Context, profileId: Long, profileName: String) {
            val intent = Intent(context, FoodLogActivity::class.java)
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