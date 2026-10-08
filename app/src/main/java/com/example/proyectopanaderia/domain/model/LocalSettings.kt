package com.example.proyectopanaderia.domain.model

data class LocalSettings(
    val rememberEmail: Boolean = true,
    val keepSession: Boolean = false,
    val savedEmail: String = "",
    val sessionEmail: String = "",
)
