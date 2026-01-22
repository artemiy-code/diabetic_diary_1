package ru.artem_torpedo.diabetesdiary.ui.reminders

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.artem_torpedo.diabetesdiary.databinding.ActivityAddReminderBinding
import java.util.Calendar

class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    private var selectedTimeMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPickTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnSave.setOnClickListener {
            // позже: сохранение в БД и AlarmManager
            finish()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                selectedTimeMillis = calendar.timeInMillis
                binding.tvTime.text = String.format("%02d:%02d", hour, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
}
