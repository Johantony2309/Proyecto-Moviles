package com.example.proyectopanaderia.presentation.shop
import com.example.proyectopanaderia.domain.repository.*
import com.example.proyectopanaderia.presentation.common.*
import kotlinx.coroutines.flow.combine

data class ShopCounts(val favorites: Int, val cart: Int)

/** Graph-scoped badges only. Each destination owns its operations and screen state. */
@dagger.hilt.android.lifecycle.HiltViewModel(assistedFactory = ShopViewModel.Factory::class)
class ShopViewModel @dagger.assisted.AssistedInject constructor(@dagger.assisted.Assisted private val email: String, private val customers: CustomerRepository,
    private val shopping: ShoppingRepository) : ScreenViewModel<ShopCounts>() {
    @dagger.assisted.AssistedFactory
    interface Factory { fun create(email: String): ShopViewModel }
    init { load() }
    fun load() = observe {
        val customerId = customers.ensureLocalCustomer(email)
        combine(shopping.favorites(customerId), shopping.cart(customerId)) { favorites, cart ->
            ShopCounts(favorites.size, cart.sumOf { it.quantity })
        }
    }
}
