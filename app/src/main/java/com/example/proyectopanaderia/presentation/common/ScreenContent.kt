package com.example.proyectopanaderia.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.presentation.components.EmptyState

@Composable
fun <T> ScreenContent(state: ScreenState<T>, retry: () -> Unit, modifier: Modifier,
    content: @Composable (T, Boolean) -> Unit) {
    when (state) {
        ScreenState.Loading -> Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is ScreenState.Error -> Column(modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EmptyState("No pudimos abrir esta pantalla", state.message)
            Button(onClick = retry) { Text("Reintentar") }
        }
        is ScreenState.Content -> content(state.data, state.busy)
    }
}
