package com.example.proyectopanaderia.presentation.login

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.example.proyectopanaderia.presentation.common.UiEvent
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

@dagger.hilt.android.lifecycle.HiltViewModel
class LoginViewModel @javax.inject.Inject constructor(private val preferences: PreferencesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(LoginState())
    val state = mutableState.asStateFlow()
    private val eventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    private var loadJob: Job? = null
    init { load() }

    fun load() {
        loadJob?.cancel()
        mutableState.update { it.copy(status = LoginStatus.LOADING, authError = null) }
        loadJob = viewModelScope.launch {
            try {
                val settings = withTimeout(5_000) { preferences.settings.first() }
                mutableState.update {
                    it.copy(status = LoginStatus.READY, settings = settings,
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
        mutableState.update { it.copy(status = LoginStatus.ERROR, busy = false) }
    }

    fun signIn(email: String, password: String) {
        if (!DemoAccount.accepts(email, password)) {
            mutableState.update { it.copy(authError = "El correo o la contraseña no coinciden. Usa la cuenta de demostración indicada abajo.") }
            return
        }
        perform {
            preferences.signedIn(DemoAccount.email)
            mutableState.update { it.copy(signedInEmail = DemoAccount.email, authError = null) }
        }
    }

    fun signOut() = perform {
        preferences.signOut()
        mutableState.update { it.copy(signedInEmail = "", authError = null) }
    }

    fun clearAuthError() { mutableState.update { it.copy(authError = null) } }

    private fun perform(action: suspend () -> Unit) {
        if (state.value.busy || state.value.status != LoginStatus.READY) return
        mutableState.update { it.copy(busy = true, authError = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                eventChannel.send(UiEvent.Message("No pudimos guardar el cambio. Inténtalo de nuevo."))
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }
}
