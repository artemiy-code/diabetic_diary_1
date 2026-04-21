package ru.artem_torpedo.diabetesdiary.ui.statistics

data class DailyNutritionPoint(
    val dayStartMillis: Long,
    val totalCalories: Float,
    val totalProtein: Float,
    val totalFat: Float,
    val totalCarbs: Float
)