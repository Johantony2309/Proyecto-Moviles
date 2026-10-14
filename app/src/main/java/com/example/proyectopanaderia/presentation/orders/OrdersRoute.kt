package com.example.proyectopanaderia.presentation.orders
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopanaderia.app.AppContainer
import com.example.proyectopanaderia.presentation.common.*

@Composable
fun OrdersRoute(email: String, container: AppContainer, onEvent: (UiEvent) -> Unit,
    onViewTicket: (Long) -> Unit, modifier: Modifier = Modifier) {
    val model: OrdersViewModel = viewModel(factory = container.ordersFactory(email))
    val state by model.state.collectAsStateWithLifecycle()
    ObserveEvents(model.events, onEvent)
    OrdersContent(state, model::load, model::setFilter, model::toggle, model::setArchived, onViewTicket, modifier)
}
