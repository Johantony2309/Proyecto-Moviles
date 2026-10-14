package com.example.proyectopanaderia.presentation.shop
import com.example.proyectopanaderia.domain.repository.*
import com.example.proyectopanaderia.presentation.common.*
import kotlinx.coroutines.flow.combine

data class ShopCounts(val favorites: Int, val cart: Int)

/** Graph-scoped badges only. Each destination owns its operations and screen state. */
class ShopViewModel(private val email: String, private val customers: CustomerRepository,
    private val shopping: ShoppingRepository) : ScreenViewModel<ShopCounts>() {
    init { load() }
    fun load() = observe {
        val customerId = customers.ensureLocalCustomer(email)
        combine(shopping.favorites(customerId), shopping.cart(customerId)) { favorites, cart ->
            ShopCounts(favorites.size, cart.sumOf { it.quantity })
        }
    }
}
