package ru.artem_torpedo.diabetesdiary.data.local.dao

import androidx.room.*
import ru.artem_torpedo.diabetesdiary.data.local.entity.ProductEntity

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAll(): List<ProductEntity>

    @Insert
    suspend fun insert(product: ProductEntity)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(products: List<ProductEntity>)

}