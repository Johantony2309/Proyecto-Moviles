package com.example.proyectopanaderia.presentation.cart
import com.example.proyectopanaderia.domain.model.CartItem
import com.example.proyectopanaderia.domain.repository.*
import com.example.proyectopanaderia.presentation.common.*

@dagger.hilt.android.lifecycle.HiltViewModel(assistedFactory = CartViewModel.Factory::class)
class CartViewModel @dagger.assisted.AssistedInject constructor(@dagger.assisted.Assisted private val email: String, private val customers: CustomerRepository,
    private val shopping: ShoppingRepository, private val orders: OrderRepository) : ScreenViewModel<List<CartItem>>() {
    @dagger.assisted.AssistedFactory
    interface Factory { fun create(email: String): CartViewModel }
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
