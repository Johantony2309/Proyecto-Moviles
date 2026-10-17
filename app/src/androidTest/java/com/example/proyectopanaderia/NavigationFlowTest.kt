package com.example.proyectopanaderia

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun tabsSettingsBackRestorationAndLogout() {
        compose.waitUntil(15_000) { exists("Iniciar sesión") || accountVisible() }
        if (accountVisible()) logout()

        compose.onNodeWithText("Configuración de este dispositivo").performScrollTo().performClick()
        compose.onNodeWithText("A tu manera").assertExists()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("A tu manera").assertExists()
        Espresso.pressBack()
        compose.onNodeWithText("Iniciar sesión").assertExists()
        login()

        compose.onNodeWithText("Buscar productos").performTextInput("croissant")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Favoritos").performClick()
        awaitText("Tus favoritos")
        Espresso.pressBack()
        awaitText("Buscar productos")
        compose.onNodeWithText("Buscar productos").assertTextContains("croissant")
        compose.onNodeWithText("Carrito").performClick()
        awaitText("Tu carrito")
        compose.activityRule.scenario.recreate()
        awaitText("Tu carrito")
        compose.onNodeWithText("Pedidos").performClick()
        compose.waitUntil(10_000) { exists("Tus pedidos") }
        compose.onNodeWithText("Archivados").performClick()
        compose.onNodeWithText("Favoritos").performClick()
        compose.onNodeWithText("Pedidos").performClick()
        compose.onNodeWithText("Archivados").assertIsSelected()
        compose.onNodeWithContentDescription("Mi cuenta").performClick()
        compose.onNodeWithText("Configuración", substring = false).performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("A tu manera").assertExists()
        Espresso.pressBack()
        compose.onNodeWithText("Archivados").assertIsSelected()

        logout()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Iniciar sesión").assertExists()
        login()
        compose.onNodeWithText("Buscar productos").assert(SemanticsMatcher.expectValue(
            androidx.compose.ui.semantics.SemanticsProperties.EditableText,
            androidx.compose.ui.text.AnnotatedString(""),
        ))
        compose.onNodeWithText("Pedidos").performClick()
        awaitText("Todos")
        compose.onNodeWithText("Todos").assertIsSelected()
        logout()
        // Back from login must finish, never reveal the previous authenticated graph.
        Espresso.pressBackUnconditionally()
        compose.waitUntil(10_000) { compose.activityRule.scenario.state == Lifecycle.State.DESTROYED }
    }

    private fun awaitText(text: String) {
        try {
            compose.waitUntil(10_000) { exists(text) && compose.onNodeWithText(text).isDisplayed() }
        } catch (error: AssertionError) {
            throw AssertionError("Pantalla esperada: $text\n" + compose.onRoot().printToString(), error)
        }
    }

    private fun login() {
        compose.onNodeWithText("Usar datos de demostración").performScrollTo().performClick()
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        compose.waitUntil(15_000) { exists("Buscar productos") }
    }

    private fun logout() {
        compose.onNodeWithContentDescription("Mi cuenta").performClick()
        compose.onNodeWithText("Cerrar sesión").performClick()
        compose.waitUntil(10_000) { exists("Iniciar sesión") }
    }

    private fun accountVisible() = compose.onAllNodesWithContentDescription("Mi cuenta").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
    private fun exists(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
}
