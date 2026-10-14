package com.example.proyectopanaderia.app

/** All routes and argument names belong to the graph, not to visual components. */
object AppDestinations {
    const val ROOT = "bakery"
    const val LOGIN = "login"
    const val SETTINGS = "settings"
    const val SHOP = "shop"
    const val CATALOG = "catalog"
    const val FAVORITES = "favorites"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val ORDER_ID = "orderId"
    const val TICKET = "ticket/{$ORDER_ID}"

    fun ticket(orderId: Long): String {
        require(orderId > 0) { "El identificador del pedido debe ser positivo." }
        return "ticket/$orderId"
    }
}
