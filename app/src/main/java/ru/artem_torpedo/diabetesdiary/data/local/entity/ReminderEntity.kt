package ru.artem_torpedo.diabetesdiary.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [Index(value = ["profileId"])],
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val title: String,
    val note: String? = null,
    val hour: Int,
    val minute: Int,
    val repeatDaily: Boolean = true,
    val enabled: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)