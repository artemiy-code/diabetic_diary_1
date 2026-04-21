package ru.artem_torpedo.diabetesdiary.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.artem_torpedo.diabetesdiary.data.repositoriy.FoodEntryRepository
import ru.artem_torpedo.diabetesdiary.data.repositoriy.MeasurementRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class StatisticsUiState(
    val periodLabel: String,
    val avgGlucose: Float?,
    val minGlucose: Float?,
    val maxGlucose: Float?,
    val count: Int,
    val points: List<ChartPoint>,

    val avgCaloriesPerDay: Float,
    val avgProteinPerDay: Float,
    val avgFatPerDay: Float,
    val avgCarbsPerDay: Float,
    val nutritionPoints: List<DailyNutritionPoint>,
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MeasurementRepository(application)
    private val foodRepository = FoodEntryRepository(application)

    val uiState = MutableLiveData(
        StatisticsUiState(
            periodLabel = "Период: —",
            avgGlucose = null,
            minGlucose = null,
            maxGlucose = null,
            count = 0,
            points = emptyList(),
            avgCaloriesPerDay = 0f,
            avgProteinPerDay = 0f,
            avgFatPerDay = 0f,
            avgCarbsPerDay = 0f,
            nutritionPoints = emptyList()
        )
    )

    fun loadStatistics(profileId: Long, fromDate: Long, toDate: Long) {
        viewModelScope.launch {
            val measurements = repository
                .getMeasurementsByDateRange(profileId, fromDate, toDate)
                .sortedBy { it.dateTime }

            val points = measurements.map { ChartPoint(it.dateTime, it.glucoseLevel) }

            val avg = if (measurements.isNotEmpty()) {
                (measurements.sumOf { it.glucoseLevel.toDouble() } / measurements.size).toFloat()
            } else null

            val min = measurements.minByOrNull { it.glucoseLevel }?.glucoseLevel
            val max = measurements.maxByOrNull { it.glucoseLevel }?.glucoseLevel

            // ===== ПИТАНИЕ =====
            val foodEntries = foodRepository
                .getFoodLogByDateRange(profileId, fromDate, toDate)

            val totalCalories = foodEntries.sumOf {
                (it.caloriesPer100g * it.grams / 100f).toDouble()
            }.toFloat()

            val totalProtein = foodEntries.sumOf {
                (it.proteinPer100g * it.grams / 100f).toDouble()
            }.toFloat()

            val totalFat = foodEntries.sumOf {
                (it.fatPer100g * it.grams / 100f).toDouble()
            }.toFloat()

            val totalCarbs = foodEntries.sumOf {
                (it.carbsPer100g * it.grams / 100f).toDouble()
            }.toFloat()

            val days = daysInRange(fromDate, toDate)

            val avgCaloriesPerDay = totalCalories / days
            val avgProteinPerDay = totalProtein / days
            val avgFatPerDay = totalFat / days
            val avgCarbsPerDay = totalCarbs / days

            val foodByDay = foodEntries.groupBy { startOfDay(it.dateTime) }

            val nutritionPoints = foodByDay.toSortedMap().map { (dayMillis, entries) ->
                DailyNutritionPoint(
                    dayStartMillis = dayMillis,
                    totalCalories = entries.sumOf {
                        (it.caloriesPer100g * it.grams / 100f).toDouble()
                    }.toFloat(),
                    totalProtein = entries.sumOf {
                        (it.proteinPer100g * it.grams / 100f).toDouble()
                    }.toFloat(),
                    totalFat = entries.sumOf {
                        (it.fatPer100g * it.grams / 100f).toDouble()
                    }.toFloat(),
                    totalCarbs = entries.sumOf {
                        (it.carbsPer100g * it.grams / 100f).toDouble()
                    }.toFloat()
                )
            }

            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val label = "Период: ${sdf.format(Date(fromDate))} - ${sdf.format(Date(toDate))}"

            uiState.postValue(
                StatisticsUiState(
                    periodLabel = label,
                    avgGlucose = avg,
                    minGlucose = min,
                    maxGlucose = max,
                    count = measurements.size,
                    points = points,

                    avgCaloriesPerDay = avgCaloriesPerDay,
                    avgProteinPerDay = avgProteinPerDay,
                    avgFatPerDay = avgFatPerDay,
                    avgCarbsPerDay = avgCarbsPerDay,
                    nutritionPoints = nutritionPoints
                )
            )
        }
    }

    private fun startOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun daysInRange(fromDate: Long, toDate: Long): Float {
        val millisPerDay = 24L * 60L * 60L * 1000L
        val diff = toDate - fromDate
        return ((diff / millisPerDay) + 1).coerceAtLeast(1).toFloat()
    }
}