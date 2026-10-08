package com.example.proyectopanaderia.domain.auth

import java.util.Locale

/** Public, deliberately fixed credentials for this offline prototype only. */
object DemoAccount {
    const val email = "demo@panaderia.com"
    const val password = "Pan12345"
    fun normalizeEmail(value: String) = value.trim().lowercase(Locale.ROOT)
    fun accepts(email: String, password: String): Boolean =
        normalizeEmail(email) == this.email && password == this.password
    fun emailError(value: String): String? = when {
        value.isBlank() -> "Escribe tu correo electrónico."
        !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value.trim()) ->
            "Revisa el correo. Ejemplo: nombre@correo.com"
        else -> null
    }
}
