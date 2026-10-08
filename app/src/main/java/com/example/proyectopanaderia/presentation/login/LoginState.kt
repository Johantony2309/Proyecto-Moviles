package com.example.proyectopanaderia.presentation.login
import com.example.proyectopanaderia.domain.model.LocalSettings

data class LoginState(
    val loading: Boolean = true,
    val loadError: Boolean = false,
    val busy: Boolean = false,
    val settings: LocalSettings = LocalSettings(),
    val signedInEmail: String = "",
    val message: String? = null,
)
