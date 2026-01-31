package ru.artem_torpedo.diabetesdiary.data.repositoriy

import android.content.Context
import ru.artem_torpedo.diabetesdiary.data.local.AppDatabase
import ru.artem_torpedo.diabetesdiary.data.local.entity.FoodEntryEntity
import ru.artem_torpedo.diabetesdiary.data.local.entity.FoodEntryWithProduct

class FoodEntryRepository(context: Context) {

    private val dao = AppDatabase.getDatabase(context).foodEntryDao()

    suspend fun getFoodLog(profileId: Long): List<FoodEntryWithProduct> =
        dao.getFoodLog(profileId)

    suspend fun getFoodLogByDateRange(profileId: Long, from: Long, to: Long): List<FoodEntryWithProduct> =
        dao.getFoodLogByDateRange(profileId, from, to)

    suspend fun insert(entry: FoodEntryEntity) = dao.insert(entry)
    suspend fun delete(entry: FoodEntryEntity) = dao.delete(entry)
    suspend fun deleteById(entryId: Long) = dao.deleteById(entryId)
}
