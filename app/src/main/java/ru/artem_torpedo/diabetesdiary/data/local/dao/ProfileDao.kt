package ru.artem_torpedo.diabetesdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.artem_torpedo.diabetesdiary.data.local.entity.ProfileEntity

@Dao
interface ProfileDao {

    @Insert
    suspend fun insert(profile: ProfileEntity)

    @Query("SELECT * FROM profiles")
    suspend fun getAllProfiles(): List<ProfileEntity>
}