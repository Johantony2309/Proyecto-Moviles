package com.example.proyectopanaderia.domain.model

data class Category(val id: Long = 0, val name: String, val position: Int = 0)
data class Product(
    val id: Long = 0, val categoryId: Long, val name: String, val description: String,
    val priceCents: Long, val imageKey: String? = null, val available: Boolean = true,
)
data class Customer(val id: Long = 0, val name: String, val email: String)
data class CartItem(val product: Product, val quantity: Int) {
    val totalCents: Long get() = Math.multiplyExact(product.priceCents, quantity.toLong())
}
