package com.example.proyectopanaderia

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.proyectopanaderia.data.local.preferences.LocalPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalPreferencesTest {
    @get:Rule val folder = TemporaryFolder(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir)

    private fun repository(file: File, job: Job): LocalPreferences = LocalPreferences(
        PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job), produceFile = { file })
    )

    @Test fun sessionIsOptInAndEmailCanBeForgotten() = runBlocking {
        val job = SupervisorJob()
        try {
            val repository = repository(File(folder.root, "defaults.preferences_pb"), job)
            repository.signedIn(DemoAccount.email)
            assertEquals(DemoAccount.email, repository.settings.first().savedEmail)
            assertEquals("", repository.settings.first().sessionEmail)
            repository.setRememberEmail(false, DemoAccount.email)
            repository.signedIn(DemoAccount.email)
            assertEquals("", repository.settings.first().savedEmail)
        } finally { job.cancelAndJoin() }
    }

    @Test fun sessionAndPreferencesSurviveReopeningTheStore() = runBlocking {
        val file = File(folder.root, "persist.preferences_pb")
        val firstJob = SupervisorJob()
        try {
            val repository = repository(file, firstJob)
            repository.setKeepSession(true, "")
            repository.signedIn(DemoAccount.email)
        } finally { firstJob.cancelAndJoin() }
        val secondJob = SupervisorJob()
        try {
            val reopened = repository(file, secondJob)
            assertTrue(reopened.settings.first().keepSession)
            assertEquals(DemoAccount.email, reopened.settings.first().sessionEmail)
            assertEquals(DemoAccount.email, reopened.settings.first().savedEmail)
            reopened.signOut()
            assertEquals("", reopened.settings.first().sessionEmail)
            assertEquals(DemoAccount.email, reopened.settings.first().savedEmail)
        } finally { secondJob.cancelAndJoin() }
    }

    @Test fun disablingPersistenceRemovesTheStoredSession() = runBlocking {
        val job = SupervisorJob()
        try {
            val repository = repository(File(folder.root, "logout.preferences_pb"), job)
            repository.setKeepSession(true, DemoAccount.email)
            repository.setKeepSession(false, DemoAccount.email)
            assertFalse(repository.settings.first().keepSession)
            assertEquals("", repository.settings.first().sessionEmail)
            repository.setKeepSession(true, "")
            assertEquals("", repository.settings.first().sessionEmail)
        } finally { job.cancelAndJoin() }
    }
}
