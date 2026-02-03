package ru.artem_torpedo.diabetesdiary.data.local.entity

data class FoodEntryWithProduct(
    val entryId: Long,
    val profileId: Long,
    val productId: Long,
    val grams: Float,
    val dateTime: Long,
    val comment: String?,
    val productName: String,
    val caloriesPer100g: Float,
    val carbsPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float
)