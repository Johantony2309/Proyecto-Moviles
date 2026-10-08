package com.example.proyectopanaderia.domain.repository
import com.example.proyectopanaderia.domain.model.LocalSettings
import kotlinx.coroutines.flow.Flow
interface PreferencesRepository {
    val settings: Flow<LocalSettings>
    suspend fun setRememberEmail(enabled: Boolean, currentEmail: String)
    suspend fun setKeepSession(enabled: Boolean, signedInEmail: String)
    suspend fun signedIn(email: String)
    suspend fun signOut()
}
