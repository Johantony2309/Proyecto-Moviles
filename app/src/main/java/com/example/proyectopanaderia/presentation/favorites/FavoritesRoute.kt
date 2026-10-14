package com.example.proyectopanaderia.presentation.favorites
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopanaderia.app.AppContainer
import com.example.proyectopanaderia.presentation.common.*

@Composable
fun FavoritesRoute(email: String, container: AppContainer, onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier) {
    val model: FavoritesViewModel = viewModel(factory = container.favoritesFactory(email))
    val state by model.state.collectAsStateWithLifecycle()
    ObserveEvents(model.events, onEvent)
    FavoritesContent(state, model::load, model::add, model::remove, modifier)
}
