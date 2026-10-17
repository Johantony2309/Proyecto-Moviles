package com.example.proyectopanaderia
import com.example.proyectopanaderia.domain.auth.DemoAccount

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LoginFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun accessErrorsSettingsAndLogout() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Iniciar sesión").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() ||
                compose.onAllNodesWithContentDescription("Mi cuenta").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        if (compose.onAllNodesWithContentDescription("Mi cuenta").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()) {
            compose.onNodeWithContentDescription("Mi cuenta").performClick()
            compose.onNodeWithText("Cerrar sesión").performClick()
            compose.waitUntil(5_000) { compose.onAllNodesWithText("Iniciar sesión").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        }
        compose.onNodeWithText("Correo electrónico").performScrollTo()
        screenshot("login.png")
        compose.onNodeWithText("Correo electrónico").performTextClearance()
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        compose.onNodeWithText("Escribe tu correo electrónico.").assertExists()
        compose.onNodeWithText("Escribe tu contraseña.").assertExists()

        compose.onNodeWithText("Correo electrónico").performTextInput(DemoAccount.email)
        compose.onNodeWithText("Contraseña", substring = false).performTextInput("equivocada")
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        compose.onNodeWithText("El correo o la contraseña no coinciden.", substring = true).assertExists()

        compose.onNodeWithText("Usar datos de demostración").performScrollTo().performClick()
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Tu próximo antojo").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.onNodeWithContentDescription("Mi cuenta").performClick()
        compose.onNodeWithText("Configuración", substring = false).performClick()
        compose.onNodeWithText("Mantener mi sesión").performScrollTo()
        val sessionSwitch = compose.onNode(isToggleable() and hasText("Mantener mi sesión"))
        if (sessionSwitch.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ToggleableState] ==
            androidx.compose.ui.state.ToggleableState.Off) sessionSwitch.performClick()
        compose.waitUntil(5_000) {
            sessionSwitch.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ToggleableState] ==
                androidx.compose.ui.state.ToggleableState.On
        }
        screenshot("settings.png")
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Mantener mi sesión").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.onNode(isToggleable() and hasText("Mantener mi sesión")).assertIsOn()
        compose.onNodeWithText("‹ Volver").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Mi cuenta").performClick()
        compose.onNodeWithText("Cerrar sesión").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Iniciar sesión").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Iniciar sesión").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.onNodeWithText("Iniciar sesión").assertExists()
    }

    private fun screenshot(name: String) {
        compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            File(compose.activity.getExternalFilesDir(null), name).outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}
