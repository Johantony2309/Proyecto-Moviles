package com.example.proyectopanaderia

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyectopanaderia.app.BakeryApplication
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
        val container = (context as BakeryApplication).container
        val database = Room.databaseBuilder(context, BakeryDatabase::class.java, "panaderia.db").build()
        val ids = mutableListOf<Long>()
        val customerId = runBlocking { container.customers.ensureLocalCustomer(DemoAccount.email) }
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
                    .fetchSemanticsNodes().isNotEmpty()
            }
            if (exists("Iniciar sesión")) {
                compose.onNodeWithText("Usar datos de demostración").performScrollTo().performClick()
                compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
            }
            compose.waitUntil(10_000) { exists("Pedidos") }
            compose.onNodeWithText("Pedidos").performClick()
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
            compose.onNodeWithText(ticketTitle).assertExists()
            compose.activityRule.scenario.recreate()
            compose.waitUntil(10_000) { exists(ticketTitle) }
            compose.activityRule.scenario.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            compose.waitUntil(10_000) {
                compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            }
            compose.waitUntil(10_000) { exists(ticketTitle) }
            compose.onNodeWithText("Volver").performScrollTo().performClick()
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

    private fun exists(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
}
