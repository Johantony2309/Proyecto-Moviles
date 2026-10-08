package com.example.proyectopanaderia.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopanaderia.presentation.components.*
import com.example.proyectopanaderia.presentation.login.LoginScreen
import com.example.proyectopanaderia.presentation.login.LoginViewModel
import com.example.proyectopanaderia.presentation.settings.SettingsScreen
import com.example.proyectopanaderia.presentation.shop.ShopScreen

@Composable
fun BakeryApp(model: LoginViewModel, container: AppContainer) {
    val state by model.state.collectAsStateWithLifecycle()
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = settingsOpen) { settingsOpen = false; model.clearMessage() }
    if (!state.loading && !state.loadError && !settingsOpen && state.signedInEmail.isNotBlank()) {
        ShopScreen(state.signedInEmail, container, { model.clearMessage(); settingsOpen = true }, model::signOut,
            state.busy, state.message, model::clearMessage)
        return
    }
    ScreenSurface {
        when {
            state.loading -> {
                BrandHeader()
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                Text("Preparando tu experiencia…", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            state.loadError -> {
                BrandHeader()
                Heading("No pudimos abrir tus preferencias")
                Text("Inténtalo de nuevo. Tus datos guardados no se han eliminado.")
                Button(onClick = model::load, modifier = Modifier.fillMaxWidth()) { Text("Reintentar") }
            }
            settingsOpen -> SettingsScreen(state.settings, state.busy, state.message, model::rememberEmail, model::keepSession) {
                settingsOpen = false
                model.clearMessage()
            }
            else -> LoginScreen(state, model::signIn, model::clearMessage, { settingsOpen = true })
        }
    }
}
