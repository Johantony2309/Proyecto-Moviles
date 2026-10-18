package com.example.proyectopanaderia

import com.example.proyectopanaderia.domain.model.*
import com.example.proyectopanaderia.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test

class UseCasesTest {
    private val product = Product(1, 1, "Pan", "Masa madre", 150)
    private val item = OrderItem(1, 1, "Pan", 150, 2)
    private fun order(vararg items: OrderItem) = Order(1, 1, 0, OrderStatus.SAVED, "ticket", items.toList())

    @Test fun checkoutAcceptsQuantityLimits() {
        ValidateCheckoutUseCase()(listOf(CartItem(product, 1), CartItem(product, 999)))
    }

    @Test fun checkoutRejectsEmptyUnavailableAndInvalidQuantities() {
        val validate = ValidateCheckoutUseCase()
        assertThrows(IllegalArgumentException::class.java) { validate(emptyList()) }
        assertThrows(IllegalArgumentException::class.java) { validate(listOf(CartItem(product.copy(available = false), 1))) }
        for (quantity in listOf(-1, 0, 1000)) {
            assertThrows(IllegalArgumentException::class.java) { validate(listOf(CartItem(product, quantity))) }
        }
    }

    @Test fun checkoutRejectsLineAndTotalOverflow() {
        val validate = ValidateCheckoutUseCase()
        val expensive = product.copy(priceCents = Long.MAX_VALUE)
        assertThrows(ArithmeticException::class.java) { validate(listOf(CartItem(expensive, 2))) }
        assertThrows(ArithmeticException::class.java) { validate(listOf(CartItem(expensive, 1), CartItem(product, 1))) }
    }

    @Test fun editingRequiresExistingSavedOrderAndOwnedItem() {
        val validate = ValidateOrderItemChangeUseCase()
        assertThrows(IllegalArgumentException::class.java) { validate(null, 1, 1) }
        assertThrows(IllegalArgumentException::class.java) { validate(order(item).copy(status = OrderStatus.ARCHIVED), 1, 1) }
        assertThrows(IllegalArgumentException::class.java) { validate(order(item), 2, 1) }
        for (quantity in listOf(-1, 1000)) {
            assertThrows(IllegalArgumentException::class.java) { validate(order(item), 1, quantity) }
        }
    }

    @Test fun editingCannotRemoveLastItemButCanRemoveOneOfTwo() {
        val validate = ValidateOrderItemChangeUseCase()
        assertThrows(IllegalArgumentException::class.java) { validate(order(item), 1, 0) }
        validate(order(item, item.copy(id = 2)), 1, 0)
    }

    @Test fun editingUsesHistoricalPricesEvenWhenProductWasDeleted() {
        ValidateOrderItemChangeUseCase()(order(item.copy(productId = null)), 1, 999)
    }

    @Test fun editingRejectsLineAndTotalOverflow() {
        val validate = ValidateOrderItemChangeUseCase()
        val expensive = item.copy(unitPriceCents = Long.MAX_VALUE, quantity = 1)
        assertThrows(ArithmeticException::class.java) { validate(order(expensive), 1, 2) }
        assertThrows(ArithmeticException::class.java) { validate(order(expensive, item.copy(id = 2)), 2, 1) }
    }

    @Test fun catalogCombinesCategoryAndNormalizedSearch() {
        val other = product.copy(id = 2, categoryId = 2)
        val result = FilterCatalogUseCase()(listOf(product, other), listOf(Category(1, "Panes")), "  MADRE  ", 1)
        assertEquals(listOf(product), result.products)
        assertEquals(1L, result.categoryId)
        assertTrue(FilterCatalogUseCase()(listOf(product), emptyList(), "Torta", null).products.isEmpty())
    }

    @Test fun deletedCategoryResetsFilterAndBlankQueryReturnsAll() {
        val result = FilterCatalogUseCase()(listOf(product), emptyList(), "  ", 99)
        assertNull(result.categoryId)
        assertEquals(listOf(product), result.products)
    }
}
