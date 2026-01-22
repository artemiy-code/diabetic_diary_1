package ru.artem_torpedo.diabetesdiary.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.artem_torpedo.diabetesdiary.data.local.dao.MeasurementDao
import ru.artem_torpedo.diabetesdiary.data.local.dao.ProfileDao
import ru.artem_torpedo.diabetesdiary.data.local.dao.ReminderDao
import ru.artem_torpedo.diabetesdiary.data.local.entity.MeasurementEntity
import ru.artem_torpedo.diabetesdiary.data.local.entity.ProfileEntity
import ru.artem_torpedo.diabetesdiary.data.local.entity.ReminderEntity

@Database(
    entities = [
        ProfileEntity::class,
        MeasurementEntity::class,
        ReminderEntity::class,
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diabetes_diary_db"
                ).fallbackToDestructiveMigration()
                    .build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
