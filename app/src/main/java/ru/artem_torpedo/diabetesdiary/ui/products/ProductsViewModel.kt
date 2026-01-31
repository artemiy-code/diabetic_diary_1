package ru.artem_torpedo.diabetesdiary.ui.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.artem_torpedo.diabetesdiary.data.local.entity.ProductEntity
import ru.artem_torpedo.diabetesdiary.data.repositoriy.ProductRepository

class ProductsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ProductRepository(application)

    val products = MutableLiveData<List<ProductEntity>>(emptyList())

    fun load() {
        viewModelScope.launch {
            products.postValue(repo.getAll())
        }
    }

    fun add(product: ProductEntity) {
        viewModelScope.launch {
            repo.insert(product)
            products.postValue(repo.getAll())
        }
    }

    fun update(product: ProductEntity) {
        viewModelScope.launch {
            repo.update(product)
            products.postValue(repo.getAll())
        }
    }

    fun delete(product: ProductEntity) {
        viewModelScope.launch {
            repo.delete(product)
            products.postValue(repo.getAll())
        }
    }
}
