package ru.artem_torpedo.diabetesdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.artem_torpedo.diabetesdiary.data.local.entity.ReminderEntity

@Dao
interface ReminderDao {

    @Insert
    suspend fun insert(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE profileId = :profileId")
    suspend fun getReminders(profileId: Long)
            : List<ReminderEntity>
}
