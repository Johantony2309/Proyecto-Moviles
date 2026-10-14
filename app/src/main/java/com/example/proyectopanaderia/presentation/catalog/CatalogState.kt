package com.example.proyectopanaderia.presentation.catalog
import com.example.proyectopanaderia.domain.model.Category
import com.example.proyectopanaderia.domain.model.Product

data class CatalogState(val products: List<Product>, val categories: List<Category>,
    val favoriteIds: Set<Long>, val query: String, val categoryId: Long?, val catalogEmpty: Boolean)
