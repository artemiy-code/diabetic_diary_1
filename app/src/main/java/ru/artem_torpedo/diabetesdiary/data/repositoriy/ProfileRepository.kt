package ru.artem_torpedo.diabetesdiary.data.repositoriy

import android.content.Context
import ru.artem_torpedo.diabetesdiary.data.local.AppDatabase
import ru.artem_torpedo.diabetesdiary.data.local.entity.ProfileEntity

class ProfileRepository(context: Context) {

    private val profileDao =
        AppDatabase.getDatabase(context).profileDao()

    suspend fun getProfiles(): List<ProfileEntity> {
        return profileDao.getAllProfiles()
    }

    suspend fun addProfile(profile: ProfileEntity) {
        profileDao.insert(profile)
    }
}