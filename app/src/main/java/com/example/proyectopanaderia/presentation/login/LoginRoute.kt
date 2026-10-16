package com.example.proyectopanaderia.presentation.login
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectopanaderia.presentation.login.LoginViewModel
import com.example.proyectopanaderia.presentation.components.ScreenSurface

@Composable
fun LoginRoute(owner: ViewModelStoreOwner, openSettings: () -> Unit) {
    val model: LoginViewModel = hiltViewModel(viewModelStoreOwner = owner)
    val state by model.state.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf(state.settings.savedEmail) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    // Password is intentionally excluded from saved instance state and DataStore.
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val form = LoginForm(email, password, visible, submitted)
    ScreenSurface {
        LoginContent(state, form, {
            email = it.email
            password = it.password
            visible = it.passwordVisible
            submitted = it.submitted
        }, model::signIn, model::clearAuthError, openSettings)
    }
}
