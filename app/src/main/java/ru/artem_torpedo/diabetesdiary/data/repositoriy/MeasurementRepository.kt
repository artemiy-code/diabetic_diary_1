package ru.artem_torpedo.diabetesdiary.data.repositoriy

import android.content.Context
import ru.artem_torpedo.diabetesdiary.data.local.AppDatabase
import ru.artem_torpedo.diabetesdiary.data.local.entity.MeasurementEntity

class MeasurementRepository(context: Context) {

    private val measurementDao =
        AppDatabase.getDatabase(context).measurementDao()

    suspend fun getMeasurements(profileId: Long): List<MeasurementEntity> {
        return measurementDao.getMeasurementsByProfile(profileId)
    }

    suspend fun addMeasurement(measurement: MeasurementEntity) {
        measurementDao.insert(measurement)
    }

    suspend fun deleteMeasurement(measurement: MeasurementEntity) {
        measurementDao.delete(measurement)
    }


    suspend fun getMeasurementsByDateRange(
        profileId: Long,
        fromDate: Long,
        toDate: Long
    ): List<MeasurementEntity> {
        return measurementDao.getMeasurementsByDateRange(
            profileId,
            fromDate,
            toDate
        )
    }

}
