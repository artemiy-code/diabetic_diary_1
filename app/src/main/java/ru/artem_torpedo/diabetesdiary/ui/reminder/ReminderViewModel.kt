package ru.artem_torpedo.diabetesdiary.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.artem_torpedo.diabetesdiary.data.local.entity.ReminderEntity
import ru.artem_torpedo.diabetesdiary.data.repositoriy.ReminderRepository

class ReminderViewModel(application: Application)
    : AndroidViewModel(application) {

    private val repository =
        ReminderRepository(application)

    fun addReminder(
        profileId: Long,
        title: String,
        timeMillis: Long
    ) {
        viewModelScope.launch {
            repository.addReminder(
                ReminderEntity(
                    profileId = profileId,
                    title = title,
                    timeMillis = timeMillis
                )
            )
        }
    }
}
