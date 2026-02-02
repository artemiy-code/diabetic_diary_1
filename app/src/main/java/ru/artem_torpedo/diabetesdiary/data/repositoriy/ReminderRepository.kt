package ru.artem_torpedo.diabetesdiary.data.repositoriy

import android.content.Context
import ru.artem_torpedo.diabetesdiary.data.local.AppDatabase
import ru.artem_torpedo.diabetesdiary.data.local.entity.ReminderEntity

class ReminderRepository(context: Context) {
    private val dao = AppDatabase.getDatabase(context).reminderDao()

    suspend fun getByProfile(profileId: Long): List<ReminderEntity> = dao.getByProfile(profileId)
    suspend fun getById(id: Long): ReminderEntity? = dao.getById(id)
    suspend fun insert(reminder: ReminderEntity): Long = dao.insert(reminder)
    suspend fun update(reminder: ReminderEntity) = dao.update(reminder)
    suspend fun delete(reminder: ReminderEntity) = dao.delete(reminder)
}
