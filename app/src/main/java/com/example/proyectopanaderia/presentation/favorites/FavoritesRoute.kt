package com.example.proyectopanaderia.presentation.favorites
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectopanaderia.presentation.common.*

@Composable
fun FavoritesRoute(email: String, onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier) {
    val model = hiltViewModel<FavoritesViewModel, FavoritesViewModel.Factory>(creationCallback = { it.create(email) })
    val state by model.state.collectAsStateWithLifecycle()
    ObserveEvents(model.events, onEvent)
    FavoritesContent(state, model::load, model::add, model::remove, modifier)
}
