package ru.artem_torpedo.diabetesdiary.data.repositoriy

import android.content.Context
import ru.artem_torpedo.diabetesdiary.data.local.AppDatabase
import ru.artem_torpedo.diabetesdiary.data.local.entity.ReminderEntity

class ReminderRepository(context: Context) {

    private val dao =
        AppDatabase.getDatabase(context).reminderDao()

    suspend fun addReminder(reminder: ReminderEntity) {
        dao.insert(reminder)
    }

    suspend fun getReminders(profileId: Long): List<ReminderEntity> {
        return dao.getReminders(profileId)
    }
}
