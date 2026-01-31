package ru.artem_torpedo.diabetesdiary.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_entries",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("profileId"),
        Index("productId"),
        Index("dateTime")
    ]
)
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val profileId: Long,
    val productId: Long,

    val grams: Float,
    val dateTime: Long = System.currentTimeMillis(),

    val comment: String? = null
)
