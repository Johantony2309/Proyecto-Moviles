package com.example.proyectopanaderia.presentation.login

data class LoginForm(val email: String = "", val password: String = "",
    val passwordVisible: Boolean = false, val submitted: Boolean = false)
