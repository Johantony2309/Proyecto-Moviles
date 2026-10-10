package com.example.proyectopanaderia.presentation.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectopanaderia.domain.model.*
import com.example.proyectopanaderia.domain.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class ShopData(
    val products: List<Product> = emptyList(), val categories: List<Category> = emptyList(),
    val cart: List<CartItem> = emptyList(), val favorites: List<Product> = emptyList(),

)
data class ShopState(
    val data: ShopData = ShopData(), val loading: Boolean = true, val loadFailed: Boolean = false,
    val busy: Boolean = false, val message: String? = null, val createdOrderId: Long? = null,
)
class ShopViewModel(
    private val email: String, private val customers: CustomerRepository,
    private val catalog: CatalogRepository, private val shopping: ShoppingRepository,
    private val orders: OrderRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ShopState())
    val state = mutableState.asStateFlow()
    private var customerId = 0L
    private var observer: Job? = null
    init { load() }
    fun load() {
        observer?.cancel()
        mutableState.update { it.copy(loading = true, loadFailed = false) }
        observer = viewModelScope.launch {
            try {
                customerId = customers.ensureLocalCustomer(email)
                combine(catalog.products(), catalog.categories(), shopping.cart(customerId),
                    shopping.favorites(customerId)) { p, c, cart, f ->
                    ShopData(p, c, cart, f)
                }.collect { data -> mutableState.update { it.copy(data = data, loading = false, loadFailed = false) } }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableState.update { it.copy(loading = false, loadFailed = true, message = "No pudimos abrir los datos. Inténtalo de nuevo.") } }
        }
    }
    fun clearMessage() { mutableState.update { it.copy(message = null) } }
    fun examples() = change("Catálogo de ejemplo preparado.") { catalog.loadExampleCatalog() }
    fun add(productId: Long) = change("Producto añadido al carrito.") { shopping.changeQuantity(customerId, productId, 1) }
    fun setQuantity(productId: Long, quantity: Int) = change {
        shopping.setQuantity(customerId, productId, quantity)
    }
    fun favorite(productId: Long, enabled: Boolean) = change {
        shopping.setFavorite(customerId, productId, enabled)
    }
    /** Confirms the current cart as an order using the existing atomic repository operation. */
    fun confirmOrder() {
        if (state.value.busy || state.value.loading || state.value.loadFailed) return
        if (state.value.data.cart.isEmpty()) {
            mutableState.update { it.copy(message = "Agrega al menos un producto antes de confirmar el pedido.") }
            return
        }
        mutableState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                val orderId = orders.createFromCart(customerId)
                mutableState.update {
                    it.copy(createdOrderId = orderId,
                        message = "Pedido #$orderId confirmado. Puedes consultarlo en Pedidos.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                mutableState.update { it.copy(message = e.message ?: "Revisa el carrito antes de confirmar.") }
            } catch (_: Exception) {
                mutableState.update { it.copy(message = "No se pudo confirmar el pedido. Inténtalo de nuevo.") }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }
    fun clearCreatedOrder() { mutableState.update { it.copy(createdOrderId = null) } }
    private fun change(message: String? = null, operation: suspend () -> Unit) {
        if (state.value.busy || state.value.loading || state.value.loadFailed) return
        mutableState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try { operation(); mutableState.update { it.copy(message = message) } }
            catch (e: CancellationException) { throw e }
            catch (e: IllegalArgumentException) { mutableState.update { it.copy(message = e.message ?: "Revisa los datos.") } }
            catch (_: Exception) { mutableState.update { it.copy(message = "No se pudo guardar el cambio. Inténtalo de nuevo.") } }
            finally { mutableState.update { it.copy(busy = false) } }
        }
    }
}
