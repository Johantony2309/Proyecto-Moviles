package com.example.proyectopanaderia.data.repository
import androidx.room.withTransaction
import com.example.proyectopanaderia.data.local.database.BakeryDatabase
import com.example.proyectopanaderia.data.local.database.entity.*
import com.example.proyectopanaderia.domain.model.OrderStatus
import com.example.proyectopanaderia.domain.repository.OrderRepository
import com.example.proyectopanaderia.domain.usecase.ValidateCheckoutUseCase
import com.example.proyectopanaderia.domain.usecase.ValidateOrderItemChangeUseCase
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomOrderRepository @javax.inject.Inject constructor(
    private val db: BakeryDatabase,
    private val validateCheckout: ValidateCheckoutUseCase = ValidateCheckoutUseCase(),
    private val validateOrderItemChange: ValidateOrderItemChangeUseCase = ValidateOrderItemChangeUseCase(),
) : OrderRepository {
    private val dao = db.orderDao()
    override fun orders(customerId: Long, status: OrderStatus?) =
        dao.orders(customerId, status).map { list -> list.map { it.toModel() } }
    override fun order(customerId: Long, orderId: Long) =
        dao.order(customerId, orderId).map { it?.toModel() }

    override suspend fun createFromCart(customerId: Long): Long = db.withTransaction {
        val cart = db.shoppingDao().cartOnce(customerId)
        validateCheckout(cart.map { it.toModel() })
        val id = dao.insert(OrderEntity(customerId = customerId, createdAt = System.currentTimeMillis(), ticketToken = UUID.randomUUID().toString()))
        dao.insertItems(cart.map {
            OrderItemEntity(orderId = id, productId = it.product.id, productName = it.product.name,
                unitPriceCents = it.product.priceCents, quantity = it.item.quantity)
        })
        db.shoppingDao().clearCart(customerId)
        id
    }

    override suspend fun updateItemQuantity(customerId: Long, orderId: Long, itemId: Long, quantity: Int) {
        db.withTransaction {
            val order = dao.orderOnce(customerId, orderId)
            validateOrderItemChange(order?.toModel(), itemId, quantity)
            if (quantity == 0) {
                dao.deleteItem(orderId, itemId)
            } else {
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
