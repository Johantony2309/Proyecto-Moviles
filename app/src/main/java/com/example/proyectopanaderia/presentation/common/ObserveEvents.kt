package com.example.proyectopanaderia.presentation.common

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

/** Single Route consumer; buffered effects wait while the destination is not resumed. */
@Composable
fun ObserveEvents(events: Flow<UiEvent>, onEvent: (UiEvent) -> Unit) {
    val owner = LocalLifecycleOwner.current
    val handler by rememberUpdatedState(onEvent)
    LaunchedEffect(events, owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            events.collect { handler(it) }
        }
    }
}
