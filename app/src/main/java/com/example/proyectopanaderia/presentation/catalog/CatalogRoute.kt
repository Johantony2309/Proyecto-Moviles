package com.example.proyectopanaderia.presentation.catalog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopanaderia.app.AppContainer
import com.example.proyectopanaderia.presentation.common.*

@Composable
fun CatalogRoute(email: String, container: AppContainer, onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier) {
    val model: CatalogViewModel = viewModel(factory = container.catalogFactory(email))
    val state by model.state.collectAsStateWithLifecycle()
    ObserveEvents(model.events, onEvent)
    CatalogContent(state, model::load, model::setQuery, model::setCategory, model::add, model::favorite, model::examples, modifier)
}
