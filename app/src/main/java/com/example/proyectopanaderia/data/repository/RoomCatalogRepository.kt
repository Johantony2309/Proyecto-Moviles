package com.example.proyectopanaderia.data.repository
import com.example.proyectopanaderia.data.local.database.dao.CatalogDao
import com.example.proyectopanaderia.data.local.database.entity.*
import com.example.proyectopanaderia.domain.model.*
import com.example.proyectopanaderia.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction
import com.example.proyectopanaderia.data.local.database.BakeryDatabase

class RoomCatalogRepository(private val db: BakeryDatabase) : CatalogRepository {
    private val dao = db.catalogDao()
    override suspend fun loadExampleCatalog() = db.withTransaction {
        require(dao.productCount() == 0) { "El catálogo ya contiene productos." }
        val ids = mutableMapOf<String, Long>()
        listOf("Panes", "Pasteles", "Dulces").forEachIndexed { position, name ->
            ids[name] = dao.categoryByName(name)?.id ?: dao.insertCategory(CategoryEntity(name = name, position = position))
        }
        dao.insertProduct(ProductEntity(categoryId = ids.getValue("Panes"), name = "Croissant", description = "Mantequilla · 1 unidad", priceCents = 150, imageKey = "croissant"))
        dao.insertProduct(ProductEntity(categoryId = ids.getValue("Pasteles"), name = "Torta de chocolate", description = "Mediana · 8 porciones", priceCents = 800, imageKey = "cake"))
        dao.insertProduct(ProductEntity(categoryId = ids.getValue("Panes"), name = "Pan de campo", description = "Masa madre · 500 g", priceCents = 325, imageKey = "bread"))
        dao.insertProduct(ProductEntity(categoryId = ids.getValue("Dulces"), name = "Rollo de canela", description = "Canela y glaseado", priceCents = 225, imageKey = "roll"))
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
