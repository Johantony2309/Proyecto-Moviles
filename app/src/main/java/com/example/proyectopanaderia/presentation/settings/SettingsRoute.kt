package com.example.proyectopanaderia.presentation.settings
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectopanaderia.presentation.login.LoginViewModel
import com.example.proyectopanaderia.presentation.components.ScreenSurface

@Composable
fun SettingsRoute(owner: ViewModelStoreOwner, back: () -> Unit) {
    val model: LoginViewModel = hiltViewModel(viewModelStoreOwner = owner)
    val state by model.state.collectAsStateWithLifecycle()
    ScreenSurface {
        SettingsContent(state.settings, state.busy, model::rememberEmail, model::keepSession, back)
    }
}
