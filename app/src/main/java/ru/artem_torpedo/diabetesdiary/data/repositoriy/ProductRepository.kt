package ru.artem_torpedo.diabetesdiary.data.repositoriy

import android.content.Context
import ru.artem_torpedo.diabetesdiary.data.local.AppDatabase
import ru.artem_torpedo.diabetesdiary.data.local.entity.ProductEntity

class ProductRepository(context: Context) {

    private val dao = AppDatabase.getDatabase(context).productDao()

    suspend fun getAll(): List<ProductEntity> = dao.getAll()
    suspend fun insert(product: ProductEntity) = dao.insert(product)
    suspend fun update(product: ProductEntity) = dao.update(product)
    suspend fun delete(product: ProductEntity) = dao.delete(product)
}