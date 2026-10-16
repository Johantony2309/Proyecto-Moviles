package com.example.proyectopanaderia.presentation.cart
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectopanaderia.presentation.common.*

@Composable
fun CartRoute(email: String, onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier) {
    val model = hiltViewModel<CartViewModel, CartViewModel.Factory>(creationCallback = { it.create(email) })
    val state by model.state.collectAsStateWithLifecycle()
    ObserveEvents(model.events, onEvent)
    CartContent(state, model::load, model::add, model::setQuantity, model::confirm, modifier)
}
