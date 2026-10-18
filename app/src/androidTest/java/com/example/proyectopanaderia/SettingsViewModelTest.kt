package com.example.proyectopanaderia

import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.proyectopanaderia.domain.auth.DemoAccount
import com.example.proyectopanaderia.domain.model.LocalSettings
import com.example.proyectopanaderia.domain.repository.PreferencesRepository
import com.example.proyectopanaderia.presentation.common.UiEvent
import com.example.proyectopanaderia.presentation.login.LoginViewModel
import com.example.proyectopanaderia.presentation.settings.SettingsViewModel
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsViewModelTest {
    private fun onMain(action: () -> Unit) =
        InstrumentationRegistry.getInstrumentation().runOnMainSync(action)

    @Test fun preferenceChangesKeepTheSharedSessionAndUseTheCurrentEmail() = runBlocking {
        val repository = FakePreferences()
        val store = ViewModelStore()
        lateinit var login: LoginViewModel
        lateinit var settings: SettingsViewModel
        onMain {
            login = LoginViewModel(repository)
            settings = SettingsViewModel(repository)
            store.put("login", login)
            store.put("settings", settings)
        }
        try {
            withTimeout(5_000) {
                login.state.first { !it.loading }
                settings.state.first { !it.loading }
                onMain { login.signIn(DemoAccount.email, DemoAccount.password) }
                login.state.first { it.signedInEmail == DemoAccount.email && !it.busy }
                onMain { settings.keepSession(true, login.state.value.signedInEmail) }
                settings.events.first()
                assertEquals(DemoAccount.email, repository.settings.value.sessionEmail)
                onMain { settings.keepSession(false, login.state.value.signedInEmail) }
                settings.events.first()
                login.state.first { !it.settings.keepSession }
                assertEquals("", repository.settings.value.sessionEmail)
                assertEquals(DemoAccount.email, login.state.value.signedInEmail)
                assertFalse(login.state.value.busy)
            }
        } finally { onMain { store.clear() } }
    }

    @Test fun failedSaveReportsErrorAndAllowsRetry() = runBlocking {
        val repository = FakePreferences()
        val store = ViewModelStore()
        lateinit var settings: SettingsViewModel
        onMain {
            settings = SettingsViewModel(repository)
            store.put("settings", settings)
        }
        try {
            withTimeout(5_000) {
                settings.state.first { !it.loading }
                repository.failWrites = true
                onMain { settings.keepSession(true, "") }
                val event = settings.events.first() as UiEvent.Message
                assertTrue(event.text.contains("No pudimos guardar"))
                settings.state.first { !it.busy }
                assertFalse(settings.state.value.keepSession)
                repository.failWrites = false
                onMain { settings.keepSession(true, "") }
                settings.events.first()
                settings.state.first { it.keepSession && !it.busy }
                assertEquals("", repository.settings.value.sessionEmail)
            }
        } finally { onMain { store.clear() } }
    }

    private class FakePreferences : PreferencesRepository {
        override val settings = MutableStateFlow(LocalSettings())
        var failWrites = false
        override suspend fun setRememberEmail(enabled: Boolean, currentEmail: String) {
            if (failWrites) throw IOException("Write failed")
            settings.value = settings.value.copy(rememberEmail = enabled,
                savedEmail = if (enabled) currentEmail else "")
        }
        override suspend fun setKeepSession(enabled: Boolean, signedInEmail: String) {
            if (failWrites) throw IOException("Write failed")
            settings.value = settings.value.copy(keepSession = enabled,
                sessionEmail = if (enabled) signedInEmail else "")
        }
        override suspend fun signedIn(email: String) = Unit
        override suspend fun signOut() = Unit
    }
}
