package com.example.proyectopanaderia.presentation.cart
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopanaderia.app.AppContainer
import com.example.proyectopanaderia.presentation.common.*

@Composable
fun CartRoute(email: String, container: AppContainer, onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier) {
    val model: CartViewModel = viewModel(factory = container.cartFactory(email))
    val state by model.state.collectAsStateWithLifecycle()
    ObserveEvents(model.events, onEvent)
    CartContent(state, model::load, model::add, model::setQuantity, model::confirm, modifier)
}
