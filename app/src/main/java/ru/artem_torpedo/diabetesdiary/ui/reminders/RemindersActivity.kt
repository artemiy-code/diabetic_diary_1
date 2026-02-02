package ru.artem_torpedo.diabetesdiary.ui.reminders

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
import java.util.Locale

class RemindersActivity : AppCompatActivity() {

    private val viewModel: RemindersViewModel by viewModels()

    private var profileId: Long = -1
    private var profileName: String = ""

    private var reminderList: List<ReminderEntity> = emptyList()
    private lateinit var adapter: ArrayAdapter<String>
    private val items = mutableListOf<String>()

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

        // Тап по элементу: включить/выключить
        listView.setOnItemClickListener { _, _, position, _ ->
            val r = reminderList[position]
            val updated = r.copy(enabled = !r.enabled)

            viewModel.update(updated) {
                if (updated.enabled) {
                    ReminderScheduler.schedule(
                        context = this,
                        reminderId = updated.id,
                        profileId = updated.profileId,
                        title = updated.title,
                        note = updated.note,
                        hour = updated.hour,
                        minute = updated.minute,
                        repeatDaily = updated.repeatDaily
                    )
                } else {
                    ReminderScheduler.cancel(this, updated.id)
                }
                viewModel.load(profileId)
            }
        }

        // Long tap: удалить
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
                R.id.nav_measurements -> { MeasurementsActivity.start(this, profileId, profileName); true }
                R.id.nav_statistics -> { StatisticsActivity.start(this, profileId, profileName); true }
                R.id.nav_products -> { ProductsActivity.start(this, profileId, profileName); true }
                R.id.nav_food_log -> { FoodLogActivity.start(this, profileId, profileName); true }
                R.id.nav_reminders -> true
                else -> false
            }
        }

        viewModel.reminders.observe(this) { list ->
            reminderList = list
            items.clear()
            items.addAll(list.map { r ->
                val time = String.format(Locale.getDefault(), "%02d:%02d", r.hour, r.minute)
                val state = if (r.enabled) "Включено" else "Выключено"
                val repeat = if (r.repeatDaily) "Ежедневно" else "Однократно"
                buildString {
                    append(time).append("  ").append(r.title)
                    append("\n").append("$state, $repeat")
                    r.note?.takeIf { it.isNotBlank() }?.let { append("\n$it") }
                    append("\n(тап: вкл/выкл, удержание: удалить)")
                }
            })
            adapter.notifyDataSetChanged()
        }

        viewModel.load(profileId)
    }

    private fun showAddOrEditDialog(existing: ReminderEntity?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.reminderTitleInput)
        val noteInput = dialogView.findViewById<EditText>(R.id.reminderNoteInput)
        val timeButton = dialogView.findViewById<Button>(R.id.reminderTimeButton)
        val repeatCheck = dialogView.findViewById<CheckBox>(R.id.repeatDailyCheck)
        val enabledCheck = dialogView.findViewById<CheckBox>(R.id.enabledCheck)

        var hour = existing?.hour ?: 9
        var minute = existing?.minute ?: 0

        fun updateTimeText() {
            timeButton.text = String.format(Locale.getDefault(), "Время: %02d:%02d", hour, minute)
        }
        updateTimeText()

        titleInput.setText(existing?.title.orEmpty())
        noteInput.setText(existing?.note.orEmpty())
        repeatCheck.isChecked = existing?.repeatDaily ?: true
        enabledCheck.isChecked = existing?.enabled ?: true

        timeButton.setOnClickListener {
            TimePickerDialog(
                this,
                { _, h, m -> hour = h; minute = m; updateTimeText() },
                hour,
                minute,
                true
            ).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Новое напоминание" else "Редактировать")
            .setView(dialogView)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val t = titleInput.text.toString().trim()
                if (t.length < 2) {
                    titleInput.error = "Минимум 2 символа"
                    titleInput.requestFocus()
                    Toast.makeText(this, "Введите название напоминания", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val note = noteInput.text.toString().trim().takeIf { it.isNotBlank() }
                val repeat = repeatCheck.isChecked
                val enabled = enabledCheck.isChecked

                if (existing == null) {
                    val entity = ReminderEntity(
                        profileId = profileId,
                        title = t,
                        note = note,
                        hour = hour,
                        minute = minute,
                        repeatDaily = repeat,
                        enabled = enabled
                    )

                    viewModel.add(entity) { newId ->
                        val inserted = entity.copy(id = newId)
                        if (inserted.enabled) {
                            ReminderScheduler.schedule(
                                context = this,
                                reminderId = inserted.id,
                                profileId = inserted.profileId,
                                title = inserted.title,
                                note = inserted.note,
                                hour = inserted.hour,
                                minute = inserted.minute,
                                repeatDaily = inserted.repeatDaily
                            )
                        }
                        viewModel.load(profileId)
                    }
                } else {
                    val updated = existing.copy(
                        title = t,
                        note = note,
                        hour = hour,
                        minute = minute,
                        repeatDaily = repeat,
                        enabled = enabled
                    )

                    viewModel.update(updated) {
                        ReminderScheduler.cancel(this, updated.id)
                        if (updated.enabled) {
                            ReminderScheduler.schedule(
                                context = this,
                                reminderId = updated.id,
                                profileId = updated.profileId,
                                title = updated.title,
                                note = updated.note,
                                hour = updated.hour,
                                minute = updated.minute,
                                repeatDaily = updated.repeatDaily
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
