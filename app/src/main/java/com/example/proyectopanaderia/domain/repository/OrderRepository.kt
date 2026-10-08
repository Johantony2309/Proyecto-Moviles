package com.example.proyectopanaderia.domain.repository
import com.example.proyectopanaderia.domain.model.*
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun orders(customerId: Long, status: OrderStatus? = null): Flow<List<Order>>
    fun order(customerId: Long, orderId: Long): Flow<Order?>
    suspend fun createFromCart(customerId: Long): Long
    suspend fun updateItemQuantity(customerId: Long, orderId: Long, itemId: Long, quantity: Int)
    suspend fun setArchived(customerId: Long, orderId: Long, archived: Boolean)
    suspend fun delete(customerId: Long, orderId: Long)
    /** Adds the available products to the current cart using their current prices. */
    suspend fun repeatToCart(customerId: Long, orderId: Long)
}
