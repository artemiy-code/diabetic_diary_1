package ru.artem_torpedo.diabetesdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val glucoseLevel: Float,
    val insulinUnits: Float? = null,
    val breadUnits: Float? = null,
    val dateTime: Long,
    val comment: String? = null
)