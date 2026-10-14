package com.example.proyectopanaderia.presentation.ticket
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopanaderia.app.AppContainer

@Composable
fun TicketRoute(email: String, orderId: Long, container: AppContainer, onBack: () -> Unit,
    modifier: Modifier = Modifier) {
    val model: TicketViewModel = viewModel(factory = container.ticketFactory(email, orderId))
    val state by model.state.collectAsStateWithLifecycle()
    TicketContent(state, model::load, onBack, modifier)
}
