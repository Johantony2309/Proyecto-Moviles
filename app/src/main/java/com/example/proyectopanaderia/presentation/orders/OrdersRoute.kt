package com.example.proyectopanaderia.presentation.orders
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectopanaderia.presentation.common.*

@Composable
fun OrdersRoute(email: String, onEvent: (UiEvent) -> Unit,
    onViewTicket: (Long) -> Unit, modifier: Modifier = Modifier) {
    val model = hiltViewModel<OrdersViewModel, OrdersViewModel.Factory>(creationCallback = { it.create(email) })
    val state by model.state.collectAsStateWithLifecycle()
    ObserveEvents(model.events, onEvent)
    OrdersContent(state, model::load, model::setFilter, model::toggle, model::setArchived, onViewTicket, modifier)
}
