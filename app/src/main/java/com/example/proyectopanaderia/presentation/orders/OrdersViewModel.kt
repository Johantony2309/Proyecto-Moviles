package com.example.proyectopanaderia.presentation.orders
import androidx.lifecycle.SavedStateHandle
import com.example.proyectopanaderia.domain.model.OrderStatus
import com.example.proyectopanaderia.domain.repository.*
import com.example.proyectopanaderia.presentation.common.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
@dagger.hilt.android.lifecycle.HiltViewModel(assistedFactory = OrdersViewModel.Factory::class)
class OrdersViewModel @dagger.assisted.AssistedInject constructor(@dagger.assisted.Assisted private val email: String, private val customers: CustomerRepository,
    private val orders: OrderRepository, private val savedState: SavedStateHandle) : ScreenViewModel<OrdersState>() {
    @dagger.assisted.AssistedFactory
    interface Factory { fun create(email: String): OrdersViewModel }
    private var customerId = 0L
    init { load() }
    fun load() = observe {
        customerId = customers.ensureLocalCustomer(email)
        savedState.getStateFlow<String?>("filter", null).flatMapLatest { name ->
            val filter = name?.let(OrderStatus::valueOf)
            combine(orders.orders(customerId, filter), savedState.getStateFlow<Long?>("expandedId", null)) { list, expanded ->
                OrdersState(list, filter, expanded)
            }
        }
    }
    fun setFilter(status: OrderStatus?) {
        // Equal values do not restart collection or introduce a loading state.
        savedState["filter"] = status?.name
    }
    fun toggle(id: Long) { savedState["expandedId"] = if (savedState.get<Long>("expandedId") == id) null else id }
    fun setArchived(id: Long, archived: Boolean) = change {
        orders.setArchived(customerId, id, archived)
        UiEvent.Message(if (archived) "Pedido archivado. Lo encontrarás en Archivados."
            else "Pedido recuperado. Lo encontrarás en Guardados.")
    }
}
