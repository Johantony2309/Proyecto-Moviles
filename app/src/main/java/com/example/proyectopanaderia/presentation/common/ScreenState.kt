package com.example.proyectopanaderia.presentation.common

sealed interface ScreenState<out T> {
    data object Loading : ScreenState<Nothing>
    data class Error(val message: String) : ScreenState<Nothing>
    data class Content<T>(val data: T, val busy: Boolean = false) : ScreenState<T>
}
sealed interface UiEvent {
    data class Message(val text: String) : UiEvent
    data class OrderCreated(val orderId: Long) : UiEvent
}
