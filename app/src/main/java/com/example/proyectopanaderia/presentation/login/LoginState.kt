package com.example.proyectopanaderia.presentation.login
import com.example.proyectopanaderia.domain.model.LocalSettings

enum class LoginStatus { LOADING, READY, ERROR }

data class LoginState(
    val status: LoginStatus = LoginStatus.LOADING,
    val busy: Boolean = false,
    val settings: LocalSettings = LocalSettings(),
    val signedInEmail: String = "",
    val authError: String? = null,
) {
    val loading: Boolean get() = status == LoginStatus.LOADING
    val loadError: Boolean get() = status == LoginStatus.ERROR
}
