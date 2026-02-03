package ru.artem_torpedo.diabetesdiary.data.local.dao

import androidx.room.*
import ru.artem_torpedo.diabetesdiary.data.local.entity.ReminderEntity

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE profileId = :profileId ORDER BY hour ASC, minute ASC")
    suspend fun getByProfile(profileId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)
}