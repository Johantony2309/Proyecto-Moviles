package com.example.proyectopanaderia.data.repository
import com.example.proyectopanaderia.data.local.database.entity.*
import com.example.proyectopanaderia.data.local.database.relation.*
import com.example.proyectopanaderia.domain.model.*

internal fun CategoryEntity.toModel() = Category(id, name, position)
internal fun ProductEntity.toModel() = Product(id, categoryId, name, description, priceCents, imageKey, available)
internal fun CustomerEntity.toModel() = Customer(id, name, email)
internal fun CartWithProduct.toModel() = CartItem(product.toModel(), item.quantity)
internal fun OrderWithItems.toModel() = Order(order.id, order.customerId, order.createdAt, order.status,
    order.ticketToken, items.sortedBy { it.id }.map { OrderItem(it.id, it.productId, it.productName, it.unitPriceCents, it.quantity) })
