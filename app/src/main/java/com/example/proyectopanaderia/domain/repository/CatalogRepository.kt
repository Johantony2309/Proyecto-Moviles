package com.example.proyectopanaderia.domain.repository
import com.example.proyectopanaderia.domain.model.*
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    suspend fun loadExampleCatalog()
    fun categories(): Flow<List<Category>>
    fun products(categoryId: Long? = null, query: String = ""): Flow<List<Product>>
    suspend fun saveCategory(category: Category): Long
    suspend fun saveProduct(product: Product): Long
    suspend fun deleteCategory(id: Long)
    suspend fun deleteProduct(id: Long)
}
