package ru.artem_torpedo.diabetesdiary.ui.reminders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.artem_torpedo.diabetesdiary.R
import ru.artem_torpedo.diabetesdiary.data.local.entity.ReminderEntity
import ru.artem_torpedo.diabetesdiary.notifications.ReminderScheduler
import ru.artem_torpedo.diabetesdiary.ui.MainActivity
import ru.artem_torpedo.diabetesdiary.ui.foodlog.FoodLogActivity
import ru.artem_torpedo.diabetesdiary.ui.measurement.MeasurementsActivity
import ru.artem_torpedo.diabetesdiary.ui.products.ProductsActivity
import ru.artem_torpedo.diabetesdiary.ui.statistics.StatisticsActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RemindersActivity : AppCompatActivity() {

    private val viewModel: RemindersViewModel by viewModels()

    private var profileId: Long = -1
    private var profileName: String = ""

    private var reminderList: List<ReminderEntity> = emptyList()
    private lateinit var adapter: ArrayAdapter<String>
    private val items = mutableListOf<String>()

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestNotifPermission = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Toast.makeText(this, "Разрешите уведомления в настройках", Toast.LENGTH_LONG).show()
            }
        }

        fun ensureNotificationPermission() {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    requestNotifPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        setContentView(R.layout.activity_reminders)
        ensureNotificationPermission()

        profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1)
        profileName = intent.getStringExtra(EXTRA_PROFILE_NAME).orEmpty()
        title = "Напоминания: $profileName"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val listView: ListView = findViewById(R.id.remindersList)
        val addButton: Button = findViewById(R.id.addReminderButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        // Теперь обычный тап = редактирование
        listView.setOnItemClickListener { _, _, position, _ ->
            val reminder = reminderList[position]
            showAddOrEditDialog(existing = reminder)
        }

        // Удержание = удаление
        listView.setOnItemLongClickListener { _, _, position, _ ->
            showDeleteDialog(reminderList[position])
            true
        }

        addButton.setOnClickListener {
            showAddOrEditDialog(existing = null)
        }

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_reminders
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_measurements -> {
                    MeasurementsActivity.start(this, profileId, profileName); true
                }
                R.id.nav_statistics -> {
                    StatisticsActivity.start(this, profileId, profileName); true
                }
                R.id.nav_products -> {
                    ProductsActivity.start(this, profileId, profileName); true
                }
                R.id.nav_food_log -> {
                    FoodLogActivity.start(this, profileId, profileName); true
                }
                R.id.nav_reminders -> true
                else -> false
            }
        }

        viewModel.reminders.observe(this) { list ->
            reminderList = list
            items.clear()
            items.addAll(list.map { r ->
                val state = if (r.enabled) "Включено" else "Выключено"
                val repeat = if (r.repeatDaily) "Ежедневно" else "Однократно"

                buildString {
                    append(dateTimeFormatter.format(Date(r.triggerAtMillis)))
                    append("  ")
                    append(r.title)
                    append("\n")
                    append("$state, $repeat")
                    r.note?.takeIf { it.isNotBlank() }?.let { append("\n$it") }
                    append("\n(тап: редактировать, удержание: удалить)")
                }
            })
            adapter.notifyDataSetChanged()
        }

        viewModel.load(profileId)
    }

    private fun showAddOrEditDialog(existing: ReminderEntity?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)

        val titleInput = dialogView.findViewById<EditText>(R.id.titleEditText)
        val noteInput = dialogView.findViewById<EditText>(R.id.noteEditText)
        val dateButton = dialogView.findViewById<Button>(R.id.dateButton)
        val timeButton = dialogView.findViewById<Button>(R.id.timeButton)
        val repeatCheck = dialogView.findViewById<CheckBox>(R.id.repeatDailyCheckBox)
        val enabledCheck = dialogView.findViewById<CheckBox>(R.id.enabledCheckBox)

        val calendar = Calendar.getInstance()

        if (existing != null) {
            titleInput.setText(existing.title)
            noteInput.setText(existing.note.orEmpty())
            repeatCheck.isChecked = existing.repeatDaily
            enabledCheck.isChecked = existing.enabled
            calendar.timeInMillis = existing.triggerAtMillis
        } else {
            // Значения по умолчанию для нового напоминания
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        fun updateDateTimeText() {
            dateButton.text = "Дата: ${dateFormatter.format(calendar.time)}"
            timeButton.text = "Время: ${timeFormatter.format(calendar.time)}"
        }

        updateDateTimeText()

        dateButton.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateTimeText()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeButton.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    updateDateTimeText()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Новое напоминание" else "Редактировать напоминание")
            .setView(dialogView)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = titleInput.text.toString().trim()
                if (title.length < 2) {
                    titleInput.error = "Минимум 2 символа"
                    titleInput.requestFocus()
                    Toast.makeText(this, "Введите название напоминания", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val note = noteInput.text.toString().trim().takeIf { it.isNotBlank() }
                val repeat = repeatCheck.isChecked
                val enabled = enabledCheck.isChecked

                val triggerAtMillis = calendar.timeInMillis

                if (existing == null) {
                    val entity = ReminderEntity(
                        profileId = profileId,
                        title = title,
                        note = note,
                        triggerAtMillis = triggerAtMillis,
                        repeatDaily = repeat,
                        enabled = enabled
                    )

                    viewModel.add(entity) { newId ->
                        val inserted = entity.copy(id = newId)
                        if (inserted.enabled) {
                            ReminderScheduler.schedule(
                                context = this,
                                reminder = inserted
                            )
                        }
                        viewModel.load(profileId)
                    }
                } else {
                    val updated = existing.copy(
                        title = title,
                        note = note,
                        triggerAtMillis = triggerAtMillis,
                        repeatDaily = repeat,
                        enabled = enabled
                    )

                    viewModel.update(updated) {
                        ReminderScheduler.cancel(this, updated.id)
                        if (updated.enabled) {
                            ReminderScheduler.schedule(
                                context = this,
                                reminder = updated
                            )
                        }
                        viewModel.load(profileId)
                    }
                }

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteDialog(reminder: ReminderEntity) {
        AlertDialog.Builder(this)
            .setTitle("Удалить напоминание?")
            .setMessage(reminder.title)
            .setPositiveButton("Удалить") { _, _ ->
                ReminderScheduler.cancel(this, reminder.id)
                viewModel.delete(reminder) { viewModel.load(profileId) }
            }
            .setNegativeButton("Отмена", null)
            .show()
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

    companion object {
        private const val EXTRA_PROFILE_ID = "profile_id"
        private const val EXTRA_PROFILE_NAME = "profile_name"

        fun start(context: Context, profileId: Long, profileName: String) {
            val intent = Intent(context, RemindersActivity::class.java)
            intent.putExtra(EXTRA_PROFILE_ID, profileId)
            intent.putExtra(EXTRA_PROFILE_NAME, profileName)
            context.startActivity(intent)
        }
    }
}