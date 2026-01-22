package ru.artem_torpedo.diabetesdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val profileId: Long,
    val title: String,
    val timeMillis: Long,
    val isActive: Boolean = true
)
