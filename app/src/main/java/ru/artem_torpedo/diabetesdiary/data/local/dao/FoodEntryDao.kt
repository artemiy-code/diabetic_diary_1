package ru.artem_torpedo.diabetesdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ru.artem_torpedo.diabetesdiary.data.local.entity.FoodEntryEntity
import ru.artem_torpedo.diabetesdiary.data.local.entity.FoodEntryWithProduct

@Dao
interface FoodEntryDao {

    @Query("""
        SELECT 
            fe.id as entryId,
            fe.profileId as profileId,
            fe.productId as productId,
            fe.grams as grams,
            fe.dateTime as dateTime,
            fe.comment as comment,

            p.name as productName,
            p.caloriesPer100g as caloriesPer100g,
            p.carbsPer100g as carbsPer100g,
            p.proteinPer100g as proteinPer100g,
            p.fatPer100g as fatPer100g
        FROM food_entries fe
        INNER JOIN products p ON p.id = fe.productId
        WHERE fe.profileId = :profileId
        ORDER BY fe.dateTime DESC
    """)
    suspend fun getFoodLog(profileId: Long): List<FoodEntryWithProduct>

    @Query("""
        SELECT 
            fe.id as entryId,
            fe.profileId as profileId,
            fe.productId as productId,
            fe.grams as grams,
            fe.dateTime as dateTime,
            fe.comment as comment,

            p.name as productName,
            p.caloriesPer100g as caloriesPer100g,
            p.carbsPer100g as carbsPer100g,
            p.proteinPer100g as proteinPer100g,
            p.fatPer100g as fatPer100g
        FROM food_entries fe
        INNER JOIN products p ON p.id = fe.productId
        WHERE fe.profileId = :profileId
          AND fe.dateTime BETWEEN :fromDate AND :toDate
        ORDER BY fe.dateTime DESC
    """)


    suspend fun getFoodLogByDateRange(
        profileId: Long,
        fromDate: Long,
        toDate: Long
    ): List<FoodEntryWithProduct>

    @Insert
    suspend fun insert(entry: FoodEntryEntity)

    @Delete
    suspend fun delete(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)

}
