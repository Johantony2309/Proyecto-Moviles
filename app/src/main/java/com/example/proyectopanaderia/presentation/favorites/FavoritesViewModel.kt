package com.example.proyectopanaderia.presentation.favorites
import com.example.proyectopanaderia.domain.model.Product
import com.example.proyectopanaderia.domain.repository.*
import com.example.proyectopanaderia.presentation.common.*

class FavoritesViewModel(private val email: String, private val customers: CustomerRepository,
    private val shopping: ShoppingRepository) : ScreenViewModel<List<Product>>() {
    private var customerId = 0L
    init { load() }
    fun load() = observe {
        customerId = customers.ensureLocalCustomer(email)
        shopping.favorites(customerId)
    }
    fun add(id: Long) = change {
        shopping.changeQuantity(customerId, id, 1)
        UiEvent.Message("Producto añadido al carrito.")
    }
    fun remove(id: Long) = change { shopping.setFavorite(customerId, id, false); null }
}
