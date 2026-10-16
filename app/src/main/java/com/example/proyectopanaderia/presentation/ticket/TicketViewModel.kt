package com.example.proyectopanaderia.presentation.ticket
import com.example.proyectopanaderia.domain.model.Order
import com.example.proyectopanaderia.domain.repository.*
import com.example.proyectopanaderia.presentation.common.ScreenViewModel
import kotlinx.coroutines.flow.flowOf

@dagger.hilt.android.lifecycle.HiltViewModel(assistedFactory = TicketViewModel.Factory::class)
class TicketViewModel @dagger.assisted.AssistedInject constructor(@dagger.assisted.Assisted private val email: String, @dagger.assisted.Assisted private val orderId: Long,
    private val customers: CustomerRepository, private val orders: OrderRepository) : ScreenViewModel<Order?>() {
    @dagger.assisted.AssistedFactory
    interface Factory { fun create(email: String, orderId: Long): TicketViewModel }
    init { load() }
    fun load() = observe {
        if (orderId <= 0) flowOf(null)
        else orders.order(customers.ensureLocalCustomer(email), orderId)
    }
}
