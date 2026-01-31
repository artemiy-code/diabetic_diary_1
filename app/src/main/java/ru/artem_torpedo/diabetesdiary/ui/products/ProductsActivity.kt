package ru.artem_torpedo.diabetesdiary.ui.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.artem_torpedo.diabetesdiary.R
import ru.artem_torpedo.diabetesdiary.data.local.entity.ProductEntity
import ru.artem_torpedo.diabetesdiary.ui.MainActivity
import ru.artem_torpedo.diabetesdiary.ui.foodlog.FoodLogActivity
import ru.artem_torpedo.diabetesdiary.ui.measurement.MeasurementsActivity
import ru.artem_torpedo.diabetesdiary.ui.statistics.StatisticsActivity

class ProductsActivity : AppCompatActivity() {

    private val viewModel: ProductsViewModel by viewModels()

    private var productList: List<ProductEntity> = emptyList()
    private lateinit var adapter: ArrayAdapter<String>
    private val items = mutableListOf<String>()

    private var profileId: Long = -1
    private var profileName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1)
        profileName = intent.getStringExtra(EXTRA_PROFILE_NAME).orEmpty()
        title = "Продукты: $profileName"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val listView: ListView = findViewById(R.id.productsList)
        val addButton: Button = findViewById(R.id.addProductButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val product = productList[position]
            showAddOrEditDialog(existing = product)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val product = productList[position]
            showDeleteDialog(product)
            true
        }

        addButton.setOnClickListener {
            showAddOrEditDialog(existing = null)
        }

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_products
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
                R.id.nav_products -> true
                R.id.nav_food_log -> {
                    FoodLogActivity.start(this, profileId, profileName)
                    true
                }
                else -> false
            }
        }

        viewModel.products.observe(this) { list ->
            productList = list
            items.clear()
            items.addAll(list.map { p ->
                "${p.name}\n" +
                        "100 г: ${fmt1(p.caloriesPer100g)} ккал, " +
                        "Б ${fmt1(p.proteinPer100g)}, " +
                        "Ж ${fmt1(p.fatPer100g)}, " +
                        "У ${fmt1(p.carbsPer100g)}"
            })

            adapter.notifyDataSetChanged()
        }

        viewModel.load()
    }

    private fun showAddOrEditDialog(existing: ProductEntity?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val caloriesInput = dialogView.findViewById<EditText>(R.id.caloriesInput)
        val carbsInput = dialogView.findViewById<EditText>(R.id.carbsInput)
        val proteinInput = dialogView.findViewById<EditText>(R.id.proteinInput)
        val fatInput = dialogView.findViewById<EditText>(R.id.fatInput)

        if (existing != null) {
            nameInput.setText(existing.name)
            caloriesInput.setText(existing.caloriesPer100g.toString())
            carbsInput.setText(existing.carbsPer100g.toString())
            proteinInput.setText(existing.proteinPer100g.toString())
            fatInput.setText(existing.fatPer100g.toString())
        }

        val titleText = if (existing == null) "Новый продукт" else "Редактировать продукт"

        AlertDialog.Builder(this)
            .setTitle(titleText)
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = nameInput.text.toString().trim()
                val cal = caloriesInput.text.toString().toFloatOrNull()
                val carbs = carbsInput.text.toString().toFloatOrNull()
                val prot = proteinInput.text.toString().toFloatOrNull()
                val fat = fatInput.text.toString().toFloatOrNull()

                if (name.isBlank() || cal == null || carbs == null || prot == null || fat == null) return@setPositiveButton

                val entity = if (existing == null) {
                    ProductEntity(
                        name = name,
                        caloriesPer100g = cal,
                        carbsPer100g = carbs,
                        proteinPer100g = prot,
                        fatPer100g = fat
                    )
                } else {
                    existing.copy(
                        name = name,
                        caloriesPer100g = cal,
                        carbsPer100g = carbs,
                        proteinPer100g = prot,
                        fatPer100g = fat
                    )
                }

                if (existing == null) viewModel.add(entity) else viewModel.update(entity)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDeleteDialog(product: ProductEntity) {
        AlertDialog.Builder(this)
            .setTitle("Удалить продукт?")
            .setMessage("Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ -> viewModel.delete(product) }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun fmt1(v: Float): String = String.format(java.util.Locale.getDefault(), "%.1f", v)

    companion object {
        private const val EXTRA_PROFILE_ID = "profile_id"
        private const val EXTRA_PROFILE_NAME = "profile_name"

        fun start(context: Context, profileId: Long, profileName: String) {
            val intent = Intent(context, ProductsActivity::class.java)
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
