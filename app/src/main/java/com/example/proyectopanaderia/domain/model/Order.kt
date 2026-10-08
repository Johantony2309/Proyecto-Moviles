package com.example.proyectopanaderia.domain.model

enum class OrderStatus { SAVED, ARCHIVED }
data class OrderItem(
    val id: Long, val productId: Long?, val productName: String,
    val unitPriceCents: Long, val quantity: Int,
) {
    val totalCents: Long get() = Math.multiplyExact(unitPriceCents, quantity.toLong())
}
data class Order(
    val id: Long, val customerId: Long, val createdAt: Long, val status: OrderStatus,
    val ticketToken: String, val items: List<OrderItem>,
) {
    val totalCents: Long get() = items.fold(0L) { total, item -> Math.addExact(total, item.totalCents) }
    // Local identifier only. It is neither a payment receipt nor proof of server submission.
    val qrPayload: String get() = "panaderia:pedido:v1:$ticketToken"
}
