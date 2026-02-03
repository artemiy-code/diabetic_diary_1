package ru.artem_torpedo.diabetesdiary.ui.measurement

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.artem_torpedo.diabetesdiary.data.local.entity.MeasurementEntity
import ru.artem_torpedo.diabetesdiary.data.repositoriy.MeasurementRepository

class MeasurementViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = MeasurementRepository(application)

    val measurements = MutableLiveData<List<MeasurementEntity>>()

    fun loadMeasurements(profileId: Long) {
        viewModelScope.launch {
            measurements.postValue(
                repository.getMeasurements(profileId)
            )
        }
    }

    fun addMeasurement(
        profileId: Long,
        glucose: Float,
        insulin: Float?,
        breadUnits: Float?,
        comment: String?
    ) {
        viewModelScope.launch {
            repository.addMeasurement(
                MeasurementEntity(
                    profileId = profileId,
                    glucoseLevel = glucose,
                    insulinUnits = insulin,
                    breadUnits = breadUnits,
                    dateTime = System.currentTimeMillis(),
                    comment = comment
                )
            )
            loadMeasurements(profileId)
        }
    }

    fun deleteMeasurement(profileId: Long, measurement: MeasurementEntity) {
        viewModelScope.launch {
            repository.deleteMeasurement(measurement)
            loadMeasurements(profileId)
        }
    }


    fun loadMeasurementsByDate(
        profileId: Long,
        fromDate: Long,
        toDate: Long
    ) {
        viewModelScope.launch {
            measurements.postValue(
                repository.getMeasurementsByDateRange(
                    profileId,
                    fromDate,
                    toDate
                )
            )
        }
    }

}