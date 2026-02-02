package ru.artem_torpedo.diabetesdiary.ui.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.artem_torpedo.diabetesdiary.data.local.entity.ReminderEntity
import ru.artem_torpedo.diabetesdiary.data.repositoriy.ReminderRepository

class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ReminderRepository(application)

    val reminders = MutableLiveData<List<ReminderEntity>>(emptyList())

    fun load(profileId: Long) {
        viewModelScope.launch {
            reminders.postValue(repo.getByProfile(profileId))
        }
    }

    fun add(reminder: ReminderEntity, onInserted: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repo.insert(reminder)
            onInserted(id)
        }
    }

    fun update(reminder: ReminderEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.update(reminder)
            onDone()
        }
    }

    fun delete(reminder: ReminderEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.delete(reminder)
            onDone()
        }
    }
}
