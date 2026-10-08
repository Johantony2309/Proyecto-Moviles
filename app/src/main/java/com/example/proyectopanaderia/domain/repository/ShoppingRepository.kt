package com.example.proyectopanaderia.domain.repository
import com.example.proyectopanaderia.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    suspend fun changeQuantity(customerId: Long, productId: Long, delta: Int)
    fun cart(customerId: Long): Flow<List<CartItem>>
    fun favorites(customerId: Long): Flow<List<Product>>
    suspend fun setQuantity(customerId: Long, productId: Long, quantity: Int)
    suspend fun clearCart(customerId: Long)
    suspend fun setFavorite(customerId: Long, productId: Long, favorite: Boolean)
}
