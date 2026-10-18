package com.example.proyectopanaderia.domain.usecase

import com.example.proyectopanaderia.domain.model.CartItem
import javax.inject.Inject

/** Validate the cart snapshot read inside the repository's transaction. */
class ValidateCheckoutUseCase @Inject constructor() {
    operator fun invoke(cart: List<CartItem>) {
        require(cart.isNotEmpty()) { "Agrega al menos un producto al carrito." }
        require(cart.all { it.product.available && it.quantity in 1..999 }) {
            "Revisa los productos del carrito."
        }
        cart.fold(0L) { total, line -> Math.addExact(total, line.totalCents) }
    }
}
