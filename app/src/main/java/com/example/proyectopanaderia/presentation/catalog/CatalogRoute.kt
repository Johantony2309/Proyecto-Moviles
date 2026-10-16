package com.example.proyectopanaderia.presentation.catalog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectopanaderia.presentation.common.*

@Composable
fun CatalogRoute(email: String, onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier) {
    val model = hiltViewModel<CatalogViewModel, CatalogViewModel.Factory>(creationCallback = { it.create(email) })
    val state by model.state.collectAsStateWithLifecycle()
    ObserveEvents(model.events, onEvent)
    CatalogContent(state, model::load, model::setQuery, model::setCategory, model::add, model::favorite, model::examples, modifier)
}
