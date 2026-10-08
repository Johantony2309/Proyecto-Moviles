package com.example.proyectopanaderia.data.local.database.dao
import androidx.room.*
import com.example.proyectopanaderia.data.local.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT COUNT(*) FROM products") suspend fun productCount(): Int
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1") suspend fun categoryByName(name: String): CategoryEntity?
    @Query("SELECT * FROM categories ORDER BY position, name")
    fun categories(): Flow<List<CategoryEntity>>
    @Query("""SELECT * FROM products WHERE (:categoryId IS NULL OR categoryId = :categoryId)
        AND (instr(lower(name), lower(:query)) > 0 OR instr(lower(description), lower(:query)) > 0)
        ORDER BY name""")
    fun products(categoryId: Long?, query: String): Flow<List<ProductEntity>>
    @Query("SELECT * FROM products WHERE id = :id") suspend fun product(id: Long): ProductEntity?
    @Insert suspend fun insertCategory(value: CategoryEntity): Long
    @Update suspend fun updateCategory(value: CategoryEntity): Int
    @Insert suspend fun insertProduct(value: ProductEntity): Long
    @Update suspend fun updateProduct(value: ProductEntity): Int
    @Query("DELETE FROM categories WHERE id = :id") suspend fun deleteCategory(id: Long): Int
    @Query("DELETE FROM products WHERE id = :id") suspend fun deleteProduct(id: Long): Int
}
