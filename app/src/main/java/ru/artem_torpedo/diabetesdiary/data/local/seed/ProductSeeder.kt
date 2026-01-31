package ru.artem_torpedo.diabetesdiary.data.local.seed

import android.content.Context
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import ru.artem_torpedo.diabetesdiary.data.local.AppDatabase
import ru.artem_torpedo.diabetesdiary.data.local.entity.ProductEntity

object ProductSeeder {

    private const val PREFS = "seed_prefs"
    private const val KEY_DONE = "products_seed_done_v2"
    private const val ASSET_FILE = "products_seed.json"

    suspend fun seedIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (prefs.getBoolean(KEY_DONE, false)) return

        val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }

        val type = object : TypeToken<List<ProductSeedDto>>() {}.type
        val dtos: List<ProductSeedDto> = Gson().fromJson(json, type)

        val entities = dtos.map {
            ProductEntity(
                name = it.name.trim(),
                caloriesPer100g = it.caloriesPer100g,
                proteinPer100g = it.proteinPer100g,
                fatPer100g = it.fatPer100g,
                carbsPer100g = it.carbsPer100g
            )
        }

        val db = AppDatabase.getDatabase(context)
        db.productDao().insertAll(entities)

        prefs.edit().putBoolean(KEY_DONE, true).apply()
    }
}
