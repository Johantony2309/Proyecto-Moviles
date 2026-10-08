package com.example.proyectopanaderia

import com.example.proyectopanaderia.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class OrderModelTest {
    @Test fun figmaTicketTotalsUseIntegerCents() {
        val order = Order(24, 1, 0, OrderStatus.SAVED, "local-ticket", listOf(
            OrderItem(1, 1, "Croissant", 150, 2),
            OrderItem(2, 2, "Torta de chocolate", 800, 1),
        ))
        assertEquals(1100L, order.totalCents)
        assertEquals("panaderia:pedido:v1:local-ticket", order.qrPayload)
    }
    @Test fun historicalLineSurvivesMissingCatalogProduct() {
        val item = OrderItem(1, null, "Croissant", 150, 2)
        assertEquals(300L, item.totalCents)
        assertEquals("Croissant", item.productName)
    }
    @Test(expected = ArithmeticException::class)
    fun arithmeticOverflowIsRejectedInsteadOfCreatingANegativePrice() {
        OrderItem(1, 1, "Producto", Long.MAX_VALUE, 2).totalCents
    }
}
