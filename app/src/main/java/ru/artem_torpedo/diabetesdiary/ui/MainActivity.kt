package ru.artem_torpedo.diabetesdiary.ui

import android.Manifest
import ru.artem_torpedo.diabetesdiary.ui.profile.ProfileViewModel
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import ru.artem_torpedo.diabetesdiary.R
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.artem_torpedo.diabetesdiary.data.local.seed.ProductSeeder
import ru.artem_torpedo.diabetesdiary.ui.measurement.MeasurementsActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val profileNames = mutableListOf<String>()

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            ProductSeeder.seedIfNeeded(this@MainActivity)
        }

        listView = findViewById(R.id.profileListView)
        val addButton: Button = findViewById(R.id.addProfileButton)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            profileNames
        )

        listView.adapter = adapter

        viewModel.profiles.observe(this) { profiles ->
            profileNames.clear()
            profileNames.addAll(profiles.map { it.name })
            adapter.notifyDataSetChanged()
        }

        viewModel.loadProfiles()

        addButton.setOnClickListener {
            showAddProfileDialog()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val profile = viewModel.profiles.value?.get(position)
            profile?.let {
                MeasurementsActivity.start(this, it.id, it.name)
            }
        }

    }

    private fun showAddProfileDialog() {
        val input = EditText(this)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Новый профиль")
            .setMessage("Введите имя пациента")
            .setView(input)
            .setPositiveButton("Добавить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = input.text.toString().trim()

                if (name.length < 2) {
                    input.error = "Минимум 2 символа"
                    input.requestFocus()
                    Toast.makeText(
                        this,
                        "Введите корректное имя профиля",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                viewModel.addProfile(name)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}