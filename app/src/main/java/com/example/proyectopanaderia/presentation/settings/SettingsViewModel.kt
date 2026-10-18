package com.example.proyectopanaderia.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectopanaderia.domain.repository.PreferencesRepository
import com.example.proyectopanaderia.presentation.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class SettingsState(
    val rememberEmail: Boolean = false,
    val keepSession: Boolean = false,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val loadError: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PreferencesRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsState())
    val state = mutableState.asStateFlow()
    private val eventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    private var loadJob: Job? = null

    init { load() }

    fun load() {
        loadJob?.cancel()
        mutableState.update { it.copy(loading = true, loadError = false) }
        loadJob = viewModelScope.launch {
            try {
                val initial = withTimeout(5_000) { preferences.settings.first() }
                mutableState.update {
                    it.copy(rememberEmail = initial.rememberEmail,
                        keepSession = initial.keepSession, loading = false)
                }
                preferences.settings.collect { settings ->
                    mutableState.update {
                        it.copy(rememberEmail = settings.rememberEmail,
                            keepSession = settings.keepSession, loading = false)
                    }
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

    // The caller supplies the current shared session; this model never stores a copy.
    fun rememberEmail(enabled: Boolean, currentEmail: String) = save {
        preferences.setRememberEmail(enabled, currentEmail)
    }

    fun keepSession(enabled: Boolean, currentEmail: String) = save {
        preferences.setKeepSession(enabled, currentEmail)
    }

    private fun save(action: suspend () -> Unit) {
        if (state.value.loading || state.value.loadError || state.value.busy) return
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                action()
                eventChannel.send(UiEvent.Message("Preferencia guardada en este dispositivo."))
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
