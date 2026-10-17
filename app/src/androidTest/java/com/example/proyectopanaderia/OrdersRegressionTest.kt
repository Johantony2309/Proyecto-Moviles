package com.example.proyectopanaderia

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyectopanaderia.data.repository.RoomCustomerRepository
import com.example.proyectopanaderia.data.local.database.BakeryDatabase
import com.example.proyectopanaderia.data.local.database.entity.OrderEntity
import com.example.proyectopanaderia.data.local.database.entity.OrderItemEntity
import com.example.proyectopanaderia.domain.auth.DemoAccount
import com.example.proyectopanaderia.domain.model.OrderStatus
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class OrdersRegressionTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun repeatedFiltersAndTicketSurviveRecreation() {
        val context = compose.activity.applicationContext
        val database = Room.databaseBuilder(context, BakeryDatabase::class.java, "panaderia.db").build()
        val ids = mutableListOf<Long>()
        val customerId = runBlocking { RoomCustomerRepository(database.customerDao()).ensureLocalCustomer(DemoAccount.email) }
        try {
            // Insert only test orders: do not clear the user's cart, catalog or history.
            runBlocking {
                database.withTransaction {
                    for (status in listOf(OrderStatus.SAVED, OrderStatus.ARCHIVED)) {
                        val id = database.orderDao().insert(OrderEntity(
                            customerId = customerId, createdAt = System.currentTimeMillis(),
                            status = status, ticketToken = UUID.randomUUID().toString(),
                        ))
                        ids += id
                        database.orderDao().insertItems(listOf(OrderItemEntity(
                            orderId = id, productId = null, productName = "Prueba de restauración",
                            unitPriceCents = 150, quantity = 2,
                        )))
                    }
                }
            }
            compose.waitUntil(10_000) {
                exists("Iniciar sesión") || compose.onAllNodesWithContentDescription("Mi cuenta")
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            }
            if (exists("Iniciar sesión")) {
                compose.onNodeWithText("Usar datos de demostración").performScrollTo().performClick()
                compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
            }
            compose.waitUntil(10_000) { exists("Pedidos") }
            compose.onNodeWithText("Pedidos").performClick()
            compose.waitUntil(10_000) { compose.onNodeWithText("Todos").isDisplayed() }
            for ((filter, id) in listOf("Todos" to ids.last(), "Guardados" to ids.first(), "Archivados" to ids.last())) {
                compose.onNodeWithText(filter).performClick()
                compose.waitUntil(10_000) { exists("Pedido #$id") }
                repeat(2) {
                    compose.onNodeWithText(filter).performClick()
                    compose.onNodeWithText("Pedido #$id").assertExists()
                }
            }
            // The archived test order is the newest and therefore the first card.
            compose.onAllNodesWithText("Ver ticket QR").onFirst().performScrollTo().performClick()
            val ticketTitle = "Ticket de pedido #${ids.last()}"
            compose.waitUntil(10_000) { exists(ticketTitle) }
            compose.activityRule.scenario.recreate()
            compose.waitUntil(10_000) { exists(ticketTitle) }
            compose.activityRule.scenario.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            compose.waitUntil(10_000) {
                compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            }
            compose.waitUntil(10_000) { exists(ticketTitle) }
            compose.onNodeWithContentDescription("Mi cuenta").performClick()
            compose.onNodeWithText("Configuración", substring = false).performClick()
            compose.onNodeWithText("A tu manera").assertExists()
            androidx.test.espresso.Espresso.pressBack()
            compose.waitUntil(10_000) { exists(ticketTitle) }
            compose.onNodeWithText("Volver").performScrollTo().performClick()
            compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
            compose.onNodeWithText("Tus pedidos").assertExists()
            compose.onNodeWithText("Archivados").assertIsSelected()
            compose.onAllNodesWithText("Ver ticket QR").onFirst().performScrollTo().performClick()
            compose.waitUntil(10_000) { exists(ticketTitle) }
            androidx.test.espresso.Espresso.pressBack()
            // Navigation restores the list's scroll position; its header may be off-screen.
            compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
            compose.onNodeWithText("Tus pedidos").assertExists()
            compose.onNodeWithText("Archivados").assertIsSelected()
        } finally {
            runBlocking { ids.forEach { database.orderDao().delete(customerId, it) } }
            database.close()
            compose.activityRule.scenario.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    private fun exists(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
}
