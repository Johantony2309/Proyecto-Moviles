package com.example.proyectopanaderia.domain.usecase

import com.example.proyectopanaderia.domain.model.Order
import com.example.proyectopanaderia.domain.model.OrderStatus
import javax.inject.Inject

/** Validate historical prices using the transaction's current order snapshot. */
class ValidateOrderItemChangeUseCase @Inject constructor() {
    operator fun invoke(order: Order?, itemId: Long, quantity: Int) {
        require(quantity in 0..999) { "La cantidad debe estar entre 0 y 999." }
        requireNotNull(order) { "El pedido ya no existe." }
        require(order.status == OrderStatus.SAVED) { "Recupera el pedido archivado antes de editarlo." }
        require(order.items.any { it.id == itemId }) { "El producto no pertenece al pedido." }
        if (quantity == 0) {
            require(order.items.size > 1) { "El pedido necesita un producto; puedes eliminar el pedido completo." }
        } else {
            order.items.fold(0L) { total, line ->
                val updatedQuantity = if (line.id == itemId) quantity else line.quantity
                Math.addExact(total, Math.multiplyExact(line.unitPriceCents, updatedQuantity.toLong()))
            }
        }
    }
}
