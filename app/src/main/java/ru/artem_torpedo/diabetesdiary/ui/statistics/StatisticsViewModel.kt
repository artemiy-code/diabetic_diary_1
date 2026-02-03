package ru.artem_torpedo.diabetesdiary.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.artem_torpedo.diabetesdiary.data.repositoriy.MeasurementRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StatisticsUiState(
    val periodLabel: String,
    val avgGlucose: Float?,
    val minGlucose: Float?,
    val maxGlucose: Float?,
    val count: Int,
    val points: List<ChartPoint>
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MeasurementRepository(application)

    val uiState = MutableLiveData(
        StatisticsUiState(
            periodLabel = "Период: —",
            avgGlucose = null,
            minGlucose = null,
            maxGlucose = null,
            count = 0,
            points = emptyList()
        )
    )

    fun loadStatistics(profileId: Long, fromDate: Long, toDate: Long) {
        viewModelScope.launch {
            val list = repository.getMeasurementsByDateRange(profileId, fromDate, toDate)
                .sortedBy { it.dateTime }

            val points = list.map { ChartPoint(it.dateTime, it.glucoseLevel) }

            val avg = if (list.isNotEmpty()) {
                (list.sumOf { it.glucoseLevel.toDouble() } / list.size).toFloat()
            } else null

            val min = list.minByOrNull { it.glucoseLevel }?.glucoseLevel
            val max = list.maxByOrNull { it.glucoseLevel }?.glucoseLevel

            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val label = "Период: ${sdf.format(Date(fromDate))} - ${sdf.format(Date(toDate))}"

            uiState.postValue(
                StatisticsUiState(
                    periodLabel = label,
                    avgGlucose = avg,
                    minGlucose = min,
                    maxGlucose = max,
                    count = list.size,
                    points = points
                )
            )
        }
    }
}