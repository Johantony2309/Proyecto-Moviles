package com.example.proyectopanaderia.presentation.settings
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopanaderia.app.AppContainer
import com.example.proyectopanaderia.presentation.login.LoginViewModel
import com.example.proyectopanaderia.presentation.components.ScreenSurface

@Composable
fun SettingsRoute(container: AppContainer, owner: ViewModelStoreOwner, back: () -> Unit) {
    val model: LoginViewModel = viewModel(viewModelStoreOwner = owner, factory = container.loginFactory)
    val state by model.state.collectAsStateWithLifecycle()
    ScreenSurface {
        SettingsContent(state.settings, state.busy, model::rememberEmail, model::keepSession, back)
    }
}
