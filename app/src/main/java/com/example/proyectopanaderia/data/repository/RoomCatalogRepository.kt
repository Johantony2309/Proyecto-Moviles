package com.example.proyectopanaderia.data.repository
import com.example.proyectopanaderia.data.local.database.dao.CatalogDao
import com.example.proyectopanaderia.data.local.database.entity.*
import com.example.proyectopanaderia.domain.model.*
import com.example.proyectopanaderia.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction
import com.example.proyectopanaderia.data.local.database.BakeryDatabase
import com.example.proyectopanaderia.data.local.database.CatalogSeed

class RoomCatalogRepository @javax.inject.Inject constructor(private val db: BakeryDatabase) : CatalogRepository {
    private val dao = db.catalogDao()
    override suspend fun loadExampleCatalog() = db.withTransaction {
        CatalogSeed.categories.forEach { (categoryName, products) ->
            val position = CatalogSeed.categories.keys.indexOf(categoryName)
            val categoryId = dao.categoryByName(categoryName)?.id
                ?: dao.insertCategory(CategoryEntity(name = categoryName, position = position))
            products.forEach { product ->
                if (dao.productByName(categoryId, product.name) == null) {
                    dao.insertProduct(ProductEntity(categoryId = categoryId, name = product.name,
                        description = product.description, priceCents = product.priceCents,
                        imageKey = product.imageKey))
                }
            }
        }
        Unit
    }
    override fun categories() = dao.categories().map { list -> list.map { it.toModel() } }
    override fun products(categoryId: Long?, query: String) =
        dao.products(categoryId, query.trim()).map { list -> list.map { it.toModel() } }

    override suspend fun saveCategory(category: Category): Long {
        require(category.name.isNotBlank()) { "La categoría necesita un nombre." }
        val entity = CategoryEntity(category.id, category.name.trim(), category.position)
        if (entity.id == 0L) return dao.insertCategory(entity)
        check(dao.updateCategory(entity) == 1) { "La categoría ya no existe." }
        return entity.id
    }
    override suspend fun saveProduct(product: Product): Long {
        require(product.name.isNotBlank() && product.categoryId > 0 && product.priceCents in 0..100_000_000L) { "Revisa el nombre, la categoría y el precio." }
        Math.multiplyExact(product.priceCents, 999L)
        val entity = ProductEntity(product.id, product.categoryId, product.name.trim(), product.description.trim(),
            product.priceCents, product.imageKey, product.available)
        if (entity.id == 0L) return dao.insertProduct(entity)
        check(dao.updateProduct(entity) == 1) { "El producto ya no existe." }
        return entity.id
    }
    override suspend fun deleteCategory(id: Long) {
        check(dao.deleteCategory(id) == 1) { "La categoría ya no existe." }
    }
    override suspend fun deleteProduct(id: Long) {
        check(dao.deleteProduct(id) == 1) { "El producto ya no existe." }
    }
}
