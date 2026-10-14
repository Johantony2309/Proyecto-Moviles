package com.example.proyectopanaderia.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Shared mechanics only; each feature owns its data and repository operations. */
abstract class ScreenViewModel<T> : ViewModel() {
    private val mutableState = MutableStateFlow<ScreenState<T>>(ScreenState.Loading)
    val state: StateFlow<ScreenState<T>> = mutableState.asStateFlow()
    private val eventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = eventChannel.receiveAsFlow()
    private var observer: Job? = null
    private var operationActive = false

    protected fun observe(source: suspend () -> Flow<T>) {
        observer?.cancel()
        mutableState.value = ScreenState.Loading
        observer = viewModelScope.launch {
            try {
                source().collect { mutableState.value = ScreenState.Content(it, operationActive) }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                mutableState.value = ScreenState.Error("No pudimos abrir los datos. Inténtalo de nuevo.")
            }
        }
    }
    protected fun change(operation: suspend () -> UiEvent?) {
        if (state.value !is ScreenState.Content || operationActive) return
        operationActive = true
        setBusy(true)
        viewModelScope.launch {
            try {
                operation()?.let { eventChannel.send(it) }
            } catch (e: CancellationException) { throw e }
            catch (e: IllegalArgumentException) {
                eventChannel.send(UiEvent.Message(e.message ?: "Revisa los datos."))
            } catch (_: Exception) {
                eventChannel.send(UiEvent.Message("No se pudo guardar el cambio. Inténtalo de nuevo."))
            } finally {
                operationActive = false
                setBusy(false)
            }
        }
    }
    private fun setBusy(value: Boolean) {
        mutableState.update { if (it is ScreenState.Content) it.copy(busy = value) else it }
    }
}
