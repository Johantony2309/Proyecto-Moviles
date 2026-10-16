package com.example.proyectopanaderia.presentation.ticket
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun TicketRoute(email: String, orderId: Long, onBack: () -> Unit,
    modifier: Modifier = Modifier) {
    val model = hiltViewModel<TicketViewModel, TicketViewModel.Factory>(creationCallback = { it.create(email, orderId) })
    val state by model.state.collectAsStateWithLifecycle()
    TicketContent(state, model::load, onBack, modifier)
}
