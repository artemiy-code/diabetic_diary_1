package ru.artem_torpedo.diabetesdiary.data.local.seed

data class ProductSeedDto(
    val name: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbsPer100g: Float
)