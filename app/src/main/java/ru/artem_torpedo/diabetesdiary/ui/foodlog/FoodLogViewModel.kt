package ru.artem_torpedo.diabetesdiary.ui.foodlog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.artem_torpedo.diabetesdiary.data.local.entity.FoodEntryEntity
import ru.artem_torpedo.diabetesdiary.data.local.entity.FoodEntryWithProduct
import ru.artem_torpedo.diabetesdiary.data.local.entity.ProductEntity
import ru.artem_torpedo.diabetesdiary.data.repositoriy.FoodEntryRepository
import ru.artem_torpedo.diabetesdiary.data.repositoriy.ProductRepository

class FoodLogViewModel(application: Application) : AndroidViewModel(application) {

    private val foodRepo = FoodEntryRepository(application)
    private val productRepo = ProductRepository(application)

    val products = MutableLiveData<List<ProductEntity>>(emptyList())
    val foodLog = MutableLiveData<List<FoodEntryWithProduct>>(emptyList())

    fun loadProducts() {
        viewModelScope.launch {
            products.postValue(productRepo.getAll())
        }
    }

    fun loadFoodLog(profileId: Long) {
        viewModelScope.launch {
            foodLog.postValue(foodRepo.getFoodLog(profileId))
        }
    }

    fun loadFoodLogByDate(profileId: Long, from: Long, to: Long) {
        viewModelScope.launch {
            foodLog.postValue(foodRepo.getFoodLogByDateRange(profileId, from, to))
        }
    }

    fun addEntry(profileId: Long, productId: Long, grams: Float, comment: String?) {
        viewModelScope.launch {
            foodRepo.insert(
                FoodEntryEntity(
                    profileId = profileId,
                    productId = productId,
                    grams = grams,
                    comment = comment
                )
            )
            foodLog.postValue(foodRepo.getFoodLog(profileId))
        }
    }

    fun deleteEntry(profileId: Long, entryId: Long) {
        viewModelScope.launch {
            foodRepo.deleteById(entryId)
            foodLog.postValue(foodRepo.getFoodLog(profileId))
        }
    }



}
