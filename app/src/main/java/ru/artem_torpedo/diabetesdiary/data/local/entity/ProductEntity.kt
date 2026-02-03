package ru.artem_torpedo.diabetesdiary.data.local.entity

import androidx.room.Index
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["name"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val caloriesPer100g: Float,
    val carbsPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float
)