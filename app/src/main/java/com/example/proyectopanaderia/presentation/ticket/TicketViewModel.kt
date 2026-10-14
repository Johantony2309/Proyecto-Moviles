package com.example.proyectopanaderia.presentation.ticket
import com.example.proyectopanaderia.domain.model.Order
import com.example.proyectopanaderia.domain.repository.*
import com.example.proyectopanaderia.presentation.common.ScreenViewModel
import kotlinx.coroutines.flow.flowOf

class TicketViewModel(private val email: String, private val orderId: Long,
    private val customers: CustomerRepository, private val orders: OrderRepository) : ScreenViewModel<Order?>() {
    init { load() }
    fun load() = observe {
        if (orderId <= 0) flowOf(null)
        else orders.order(customers.ensureLocalCustomer(email), orderId)
    }
}
