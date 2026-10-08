package com.example.proyectopanaderia.data.local.preferences

import com.example.proyectopanaderia.domain.model.LocalSettings
import com.example.proyectopanaderia.domain.repository.PreferencesRepository
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.localDataStore by preferencesDataStore(name = "panaderia_local")

/** A single device's preferences. Passwords never belong in this store. */
class LocalPreferences(private val store: DataStore<Preferences>) : PreferencesRepository {
    override val settings: Flow<LocalSettings> = store.data.map { preferences ->
        LocalSettings(
            rememberEmail = preferences[RememberEmail] ?: true,
            keepSession = preferences[KeepSession] ?: false,
            savedEmail = preferences[SavedEmail].orEmpty(),
            sessionEmail = if (preferences[KeepSession] == true) {
                preferences[SessionEmail].orEmpty()
            } else "",
        )
    }

    override suspend fun setRememberEmail(enabled: Boolean, currentEmail: String) {
        store.edit {
            it[RememberEmail] = enabled
            if (enabled && currentEmail.isNotBlank()) it[SavedEmail] = currentEmail
            if (!enabled) it.remove(SavedEmail)
        }
    }

    override suspend fun setKeepSession(enabled: Boolean, signedInEmail: String) {
        store.edit {
            it[KeepSession] = enabled
            if (enabled && signedInEmail.isNotBlank()) it[SessionEmail] = signedInEmail
            else it.remove(SessionEmail)
        }
    }

    override suspend fun signedIn(email: String) {
        store.edit {
            if (it[RememberEmail] != false) it[SavedEmail] = email
            else it.remove(SavedEmail)
            if (it[KeepSession] == true) it[SessionEmail] = email
            else it.remove(SessionEmail)
        }
    }

    override suspend fun signOut() { store.edit { it.remove(SessionEmail) } }

    private companion object {
        val RememberEmail = booleanPreferencesKey("remember_email")
        val KeepSession = booleanPreferencesKey("keep_session")
        val SavedEmail = stringPreferencesKey("saved_email")
        val SessionEmail = stringPreferencesKey("session_email")
    }
}
