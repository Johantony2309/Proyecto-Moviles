package com.example.proyectopanaderia.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.IOException
import com.example.proyectopanaderia.domain.auth.DemoAccount
import com.example.proyectopanaderia.domain.repository.PreferencesRepository

class LoginViewModel(private val preferences: PreferencesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(LoginState())
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null
    init { load() }

    fun load() {
        loadJob?.cancel()
        mutableState.update { it.copy(loading = true, loadError = false, message = null) }
        loadJob = viewModelScope.launch {
            try {
                val settings = withTimeout(5_000) { preferences.settings.first() }
                mutableState.update {
                    it.copy(loading = false, settings = settings,
                        signedInEmail = settings.sessionEmail.takeIf { email -> email == DemoAccount.email }.orEmpty())
                }
                preferences.settings.collect { settings ->
                    mutableState.update { it.copy(settings = settings) }
                }
            } catch (_: TimeoutCancellationException) {
                loadFailed()
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                loadFailed()
            }
        }
    }

    private fun loadFailed() {
        mutableState.update { it.copy(loading = false, loadError = true) }
    }

    fun signIn(email: String, password: String) {
        if (!DemoAccount.accepts(email, password)) {
            mutableState.update { it.copy(message = "El correo o la contraseña no coinciden. Usa la cuenta de demostración indicada abajo.") }
            return
        }
        perform {
            preferences.signedIn(DemoAccount.email)
            mutableState.update { it.copy(signedInEmail = DemoAccount.email, message = null) }
        }
    }

    fun signOut() = perform {
        preferences.signOut()
        mutableState.update { it.copy(signedInEmail = "", message = null) }
    }

    fun rememberEmail(enabled: Boolean) = perform {
        preferences.setRememberEmail(enabled, state.value.signedInEmail)
        mutableState.update { it.copy(message = "Preferencia guardada en este dispositivo.") }
    }

    fun keepSession(enabled: Boolean) = perform {
        preferences.setKeepSession(enabled, state.value.signedInEmail)
        mutableState.update { it.copy(message = "Preferencia guardada en este dispositivo.") }
    }

    fun clearMessage() { mutableState.update { it.copy(message = null) } }

    private fun perform(action: suspend () -> Unit) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                mutableState.update { it.copy(message = "No pudimos guardar el cambio. Inténtalo de nuevo.") }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }
}
