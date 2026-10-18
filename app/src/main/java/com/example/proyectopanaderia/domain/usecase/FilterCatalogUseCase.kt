package com.example.proyectopanaderia.domain.usecase

import com.example.proyectopanaderia.domain.model.Category
import com.example.proyectopanaderia.domain.model.Product
import javax.inject.Inject

data class FilteredCatalog(val products: List<Product>, val categoryId: Long?)

class FilterCatalogUseCase @Inject constructor() {
    operator fun invoke(
        products: List<Product>, categories: List<Category>, query: String, selectedCategoryId: Long?,
    ): FilteredCatalog {
        val categoryId = selectedCategoryId?.takeIf { id -> categories.any { it.id == id } }
        val search = query.trim()
        return FilteredCatalog(products.filter {
            (categoryId == null || it.categoryId == categoryId) &&
                (it.name.contains(search, true) || it.description.contains(search, true))
        }, categoryId)
    }
}
