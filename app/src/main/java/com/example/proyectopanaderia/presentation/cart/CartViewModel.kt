package com.example.proyectopanaderia.presentation.cart
import com.example.proyectopanaderia.domain.model.CartItem
import com.example.proyectopanaderia.domain.repository.*
import com.example.proyectopanaderia.presentation.common.*

class CartViewModel(private val email: String, private val customers: CustomerRepository,
    private val shopping: ShoppingRepository, private val orders: OrderRepository) : ScreenViewModel<List<CartItem>>() {
    private var customerId = 0L
    init { load() }
    fun load() = observe {
        customerId = customers.ensureLocalCustomer(email)
        shopping.cart(customerId)
    }
    fun add(id: Long) = change { shopping.changeQuantity(customerId, id, 1); null }
    fun setQuantity(id: Long, quantity: Int) = change { shopping.setQuantity(customerId, id, quantity); null }
    fun confirm() = change { UiEvent.OrderCreated(orders.createFromCart(customerId)) }
}
