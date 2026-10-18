package com.example.proyectopanaderia.presentation.settings
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectopanaderia.presentation.common.ObserveEvents
import com.example.proyectopanaderia.presentation.common.UiEvent
import com.example.proyectopanaderia.presentation.components.ScreenSurface

@Composable
fun SettingsRoute(signedInEmail: String, onEvent: (UiEvent) -> Unit, back: () -> Unit) {
    val model: SettingsViewModel = hiltViewModel()
    val state by model.state.collectAsStateWithLifecycle()
    ObserveEvents(model.events, onEvent)
    BackHandler(enabled = state.busy) { }
    ScreenSurface {
        if (state.loading || state.loadError) {
            TextButton(onClick = back) { Text("‹ Volver") }
            if (state.loading) CircularProgressIndicator()
            else {
                Text("No pudimos abrir tus preferencias")
                Button(onClick = model::load) { Text("Reintentar") }
            }
        } else {
            SettingsContent(state, { model.rememberEmail(it, signedInEmail) },
                { model.keepSession(it, signedInEmail) }, back)
        }
    }
}
