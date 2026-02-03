package ru.artem_torpedo.diabetesdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import ru.artem_torpedo.diabetesdiary.data.local.entity.MeasurementEntity

@Dao
interface MeasurementDao {

    @Insert
    suspend fun insert(measurement: MeasurementEntity)

    @Delete
    suspend fun delete(measurement: MeasurementEntity)

    @Query("SELECT * FROM measurements WHERE profileId = :profileId ORDER BY dateTime DESC")
    suspend fun getMeasurementsByProfile(profileId: Long): List<MeasurementEntity>

    @Query(
        """
    SELECT * FROM measurements
    WHERE profileId = :profileId
    AND dateTime BETWEEN :fromDate AND :toDate
    ORDER BY dateTime DESC
"""
    )
    suspend fun getMeasurementsByDateRange(
        profileId: Long,
        fromDate: Long,
        toDate: Long
    ): List<MeasurementEntity>

}