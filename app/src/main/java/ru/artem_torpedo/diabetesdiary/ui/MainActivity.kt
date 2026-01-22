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
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import ru.artem_torpedo.diabetesdiary.notifications.ReminderNotification
import ru.artem_torpedo.diabetesdiary.ui.measurement.MeasurementsActivity
import ru.artem_torpedo.diabetesdiary.util.ReminderScheduler

class MainActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val profileNames = mutableListOf<String>()

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        ReminderNotification.createChannel(this)
//        ReminderScheduler.schedule(
//            context = this,
//            title = "Измерить сахар",
//            timeMillis = System.currentTimeMillis() + 6000
//        )


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

        AlertDialog.Builder(this)
            .setTitle("Новый профиль")
            .setMessage("Введите имя пациента")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    viewModel.addProfile(name)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }


}
