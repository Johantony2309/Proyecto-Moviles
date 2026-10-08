package com.example.proyectopanaderia.data.repository
import androidx.room.withTransaction
import com.example.proyectopanaderia.data.local.database.BakeryDatabase
import com.example.proyectopanaderia.data.local.database.entity.*
import com.example.proyectopanaderia.domain.model.OrderStatus
import com.example.proyectopanaderia.domain.repository.OrderRepository
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomOrderRepository(private val db: BakeryDatabase) : OrderRepository {
    private val dao = db.orderDao()
    override fun orders(customerId: Long, status: OrderStatus?) =
        dao.orders(customerId, status).map { list -> list.map { it.toModel() } }
    override fun order(customerId: Long, orderId: Long) =
        dao.order(customerId, orderId).map { it?.toModel() }

    override suspend fun createFromCart(customerId: Long): Long = db.withTransaction {
        val cart = db.shoppingDao().cartOnce(customerId)
        require(cart.isNotEmpty()) { "Agrega al menos un producto al carrito." }
        require(cart.all { it.product.available && it.item.quantity in 1..999 }) { "Revisa los productos del carrito." }
        cart.fold(0L) { total, line ->
            Math.addExact(total, Math.multiplyExact(line.product.priceCents, line.item.quantity.toLong()))
        }
        val id = dao.insert(OrderEntity(customerId = customerId, createdAt = System.currentTimeMillis(), ticketToken = UUID.randomUUID().toString()))
        dao.insertItems(cart.map {
            OrderItemEntity(orderId = id, productId = it.product.id, productName = it.product.name,
                unitPriceCents = it.product.priceCents, quantity = it.item.quantity)
        })
        db.shoppingDao().clearCart(customerId)
        id
    }

    override suspend fun updateItemQuantity(customerId: Long, orderId: Long, itemId: Long, quantity: Int) {
        require(quantity in 0..999) { "La cantidad debe estar entre 0 y 999." }
        db.withTransaction {
            val order = requireNotNull(dao.orderOnce(customerId, orderId)) { "El pedido ya no existe." }
            require(order.order.status == OrderStatus.SAVED) { "Recupera el pedido archivado antes de editarlo." }
            require(order.items.any { it.id == itemId }) { "El producto no pertenece al pedido." }
            if (quantity == 0) {
                require(order.items.size > 1) { "El pedido necesita un producto; puedes eliminar el pedido completo." }
                dao.deleteItem(orderId, itemId)
            } else {
                order.items.fold(0L) { total, line ->
                    Math.addExact(total, Math.multiplyExact(line.unitPriceCents, (if (line.id == itemId) quantity else line.quantity).toLong()))
                }
                dao.updateQuantity(orderId, itemId, quantity)
            }
        }
    }

    override suspend fun setArchived(customerId: Long, orderId: Long, archived: Boolean) {
        check(dao.setStatus(customerId, orderId, if (archived) OrderStatus.ARCHIVED else OrderStatus.SAVED) == 1) { "El pedido ya no existe." }
    }
    override suspend fun delete(customerId: Long, orderId: Long) {
        check(dao.delete(customerId, orderId) == 1) { "El pedido ya no existe." }
    }

    override suspend fun repeatToCart(customerId: Long, orderId: Long) {
        db.withTransaction {
            val order = requireNotNull(dao.orderOnce(customerId, orderId)) { "El pedido ya no existe." }
            require(order.items.isNotEmpty()) { "El pedido está vacío." }
            val quantities = db.shoppingDao().cartOnce(customerId).associate { it.item.productId to it.item.quantity }.toMutableMap()
            for (line in order.items) {
                val productId = requireNotNull(line.productId) { "Un producto del pedido ya no existe." }
                val product = requireNotNull(db.catalogDao().product(productId)) { "Un producto del pedido ya no existe." }
                require(product.available) { "Un producto del pedido no está disponible." }
                val quantity = Math.addExact(quantities[productId] ?: 0, line.quantity)
                require(quantity in 1..999) { "El carrito supera la cantidad permitida." }
                quantities[productId] = quantity
                db.shoppingDao().putCartItem(CartItemEntity(customerId, productId, quantity))
            }
        }
    }
}
