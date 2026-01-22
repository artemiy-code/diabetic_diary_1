package ru.artem_torpedo.diabetesdiary.ui.reminders

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.artem_torpedo.diabetesdiary.databinding.ActivityRemindersBinding

class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddReminder.setOnClickListener {
            startActivity(
                Intent(this, AddReminderActivity::class.java)
            )
        }
    }
}
