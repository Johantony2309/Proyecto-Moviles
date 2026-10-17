package com.example.proyectopanaderia

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.EntryPoints
import com.example.proyectopanaderia.domain.auth.DemoAccount
import com.example.proyectopanaderia.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Explicit multi-process check:
 * -e phase4 prepare -> force-stop, airplane mode -> -e phase4 offline
 * -e phase4 layout (with large system font) -> -e phase4 cleanup.
 * The fixture journal survives process death so cleanup can restore the original cart.
 */
@RunWith(AndroidJUnit4::class)
class OfflineFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val container get() = EntryPoints.get(compose.activity.application, OfflineTestRepositories::class.java)
    private val journal get() = File(compose.activity.filesDir, "phase4-fixture.json")

    @Test fun fullFlowAndOfflinePersistence() {
        val phase = InstrumentationRegistry.getArguments().getString("phase4")
        assumeTrue("Run explicitly with -e phase4 prepare/offline/layout/cleanup", phase != null)
        when (phase) {
            "prepare" -> prepareAndPurchase()
            "offline" -> checkPersistedData()
            "layout" -> checkLayouts()
            "cleanup" -> cleanup()
            else -> error("Unknown phase: $phase")
        }
    }

    private fun prepareAndPurchase() {
        check(!journal.exists()) { "Run phase4 cleanup before preparing another fixture." }
        val fixture = runBlocking {
            val customer = container.customers.ensureLocalCustomer(DemoAccount.email)
            val original = container.shopping.cart(customer).first()
            val preferences = container.preferences.settings.first()
            JSONObject().put("customer", customer).put("keepSession", preferences.keepSession)
                .put("name", "Pan prueba offline ${System.currentTimeMillis()}")
                .put("cart", JSONArray().apply {
                    original.forEach { put(JSONObject().put("id", it.product.id).put("quantity", it.quantity)) }
                }).also { journal.writeText(it.toString()) }
        }
        runBlocking {
            val category = container.catalog.saveCategory(Category(name = fixture.getString("name")))
            fixture.put("category", category); save(fixture)
            val product = container.catalog.saveProduct(Product(categoryId = category,
                name = fixture.getString("name"), description = "Validación local", priceCents = 150))
            fixture.put("product", product); save(fixture)
            container.shopping.clearCart(fixture.getLong("customer"))
        }
        login()
        val name = fixture.getString("name")
        val product = fixture.getLong("product")
        compose.onNodeWithText("Catálogo").performClick()
        awaitText("Buscar productos")
        compose.onNodeWithText("Buscar productos").performTextReplacement(name)
        Espresso.closeSoftKeyboard()
        compose.onNode(hasScrollToIndexAction() and SemanticsMatcher.keyIsDefined(
            androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange
        )).performScrollToNode(hasTestTag("product-$product"))
        compose.onNode(hasText("♡ Guardar") and hasAnyAncestor(hasTestTag("product-$product"))).performClick()
        compose.waitUntil(10_000) {
            runBlocking { container.shopping.favorites(fixture.getLong("customer")).first().any { it.id == product } }
        }
        addFromFavorites(fixture)
        compose.onNodeWithText("Carrito").performClick()
        awaitText("Tu carrito")
        compose.waitUntil(10_000) { !exists("Producto añadido al carrito.") }
        compose.onNodeWithText("Confirmar pedido").performScrollTo().performClick()
        awaitText("Tus pedidos")
        val order = runBlocking {
            container.orders.orders(fixture.getLong("customer")).first { list ->
                list.any { it.items.any { item -> item.productId == product } }
            }.first { it.items.any { item -> item.productId == product } }
        }
        fixture.put("order", order.id).put("token", order.ticketToken); save(fixture)
        assertEquals(150L, order.totalCents)
        assertTrue(runBlocking { container.shopping.cart(fixture.getLong("customer")).first().isEmpty() })
        openTicket(order.id)
        screenshot("phase4-ticket")
        // Keep a new, unconfirmed cart alongside the confirmed order and favorite.
        addFromFavorites(fixture)
        runBlocking { container.preferences.setKeepSession(true, DemoAccount.email) }
        assertPersisted(fixture)
        screenshot("phase4-before-stop")
    }

    private fun checkPersistedData() {
        val fixture = read()
        assertEquals("Airplane mode must be enabled outside the app", 1,
            Settings.Global.getInt(compose.activity.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0))
        // Do not log in here: persisted session must reopen automatically.
        awaitText("Buscar productos")
        assertPersisted(fixture)
        compose.onNodeWithText("Favoritos").performClick()
        awaitText("Tus favoritos")
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasTestTag("favorite-${fixture.getLong("product")}"))
        compose.onNodeWithText(fixture.getString("name")).assertIsDisplayed()
        screenshot("phase4-offline-favorites")
        compose.onNodeWithText("Carrito").performClick()
        awaitText("Tu carrito")
        compose.onNodeWithText(fixture.getString("name")).assertExists()
        compose.onNodeWithText("Cantidad: 1").assertExists()
        screenshot("phase4-offline-cart")
        compose.onNodeWithText("Pedidos").performClick()
        awaitText("Tus pedidos")
        openTicket(fixture.getLong("order"))
        screenshot("phase4-offline-ticket")
    }

    private fun checkLayouts() {
        val fixture = read()
        assertTrue("Run layout with system font_scale >= 1.5", compose.activity.resources.configuration.fontScale >= 1.5f)
        login()
        compose.onNodeWithText("Catálogo").performClick()
        awaitText("Buscar productos")
        compose.onNodeWithText("Buscar productos").performTextReplacement(fixture.getString("name"))
        compose.onNodeWithText("Buscar productos").performClick()
        compose.waitUntil(10_000) {
            ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        compose.onNodeWithText("Buscar productos").assertIsDisplayed()
        screenshot("phase4-keyboard")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Carrito").performClick()
        awaitText("Tu carrito")
        compose.onNodeWithText("Confirmar pedido").performScrollTo().assertIsDisplayed()
        screenshot("phase4-large-text-cart")
        compose.onNodeWithText("Catálogo").performClick()
        awaitText("Buscar productos")
        compose.activityRule.scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        compose.waitUntil(10_000) {
            compose.activity.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        }
        awaitText("Buscar productos")
        compose.onNodeWithText("Buscar productos").assertTextContains(fixture.getString("name"))
        // Compose can be idle while Android is still animating the rotated window.
        android.os.SystemClock.sleep(1_000)
        screenshot("phase4-landscape-catalog")
        compose.onNodeWithText("Carrito").performClick()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        awaitText("Tu carrito")
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Confirmar pedido"))
        compose.onNodeWithText("Confirmar pedido").performScrollTo().assertIsDisplayed()
        screenshot("phase4-landscape-cart")
        compose.onNodeWithText("Pedidos").performClick()
        awaitText("Tus pedidos")
        compose.onNodeWithText("Archivados").assertIsDisplayed()
        openTicket(fixture.getLong("order"))
        screenshot("phase4-landscape-ticket")
        compose.activityRule.scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        compose.waitUntil(10_000) {
            compose.activity.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
        }
    }

    private fun assertPersisted(f: JSONObject) = runBlocking {
        val customer = f.getLong("customer")
        val product = f.getLong("product")
        assertTrue(container.shopping.favorites(customer).first().any { it.id == product })
        val line = container.shopping.cart(customer).first().single { it.product.id == product }
        assertEquals(1, line.quantity)
        val order = container.orders.order(customer, f.getLong("order")).first()!!
        assertEquals(f.getString("token"), order.ticketToken)
        assertEquals(150L, order.totalCents)
        assertEquals(product, order.items.single().productId)
    }

    private fun addFromFavorites(f: JSONObject) {
        compose.onNodeWithText("Favoritos").performClick()
        awaitText("Tus favoritos")
        val tag = "favorite-${f.getLong("product")}"
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasTestTag(tag))
        compose.onNode(hasText("Añadir al carrito") and hasAnyAncestor(hasTestTag(tag))).performScrollTo().performClick()
        compose.waitUntil(10_000) {
            runBlocking { container.shopping.cart(f.getLong("customer")).first().any { it.product.id == f.getLong("product") } }
        }
    }

    private fun openTicket(id: Long) {
        awaitText("Tus pedidos")
        compose.onNodeWithText("Todos").performClick()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasTestTag("order-$id"))
        compose.onNode(hasText("Ver ticket QR") and hasAnyAncestor(hasTestTag("order-$id"))).performScrollTo().performClick()
        compose.waitUntil(10_000) { exists("Ticket de pedido #$id") }
        val localNotice = compose.activity.getString(R.string.local_order_notice)
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(localNotice))
        compose.onNodeWithText(localNotice).performScrollTo().assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription("Código QR del pedido"))
        compose.onNodeWithContentDescription("Código QR del pedido").performScrollTo().assertIsDisplayed()
        val expected = runBlocking {
            container.orders.order(read().getLong("customer"), id).first()!!.qrPayload
        }
        // Semantics can settle before the system compositor publishes the new frame.
        try {
            compose.waitUntil(10_000) {
                val bitmap = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                val source = com.google.zxing.RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
                bitmap.recycle()
                val decoded = runCatching { com.google.zxing.qrcode.QRCodeReader().decode(
                    com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))) }.getOrNull()
                decoded?.text == expected
            }
        } catch (error: ComposeTimeoutException) {
            screenshot("phase4-qr-failure")
            throw AssertionError("The visible QR must be complete and readable", error)
        }
    }

    private fun login() {
        compose.waitUntil(15_000) { exists("Iniciar sesión") || exists("Catálogo") }
        if (exists("Iniciar sesión")) {
            compose.onNodeWithText("Usar datos de demostración").performScrollTo().performClick()
            compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        }
        awaitText("Catálogo")
    }

    private fun cleanup() = runBlocking {
        if (!journal.exists()) return@runBlocking
        val f = read()
        val customer = f.getLong("customer")
        if (f.has("product")) {
            val product = f.getLong("product")
            container.orders.orders(customer).first()
                .filter { it.items.any { item -> item.productId == product } }
                .forEach { container.orders.delete(customer, it.id) }
            container.catalog.deleteProduct(product)
        }
        if (f.has("category")) container.catalog.deleteCategory(f.getLong("category"))
        val cart = f.getJSONArray("cart")
        for (i in 0 until cart.length()) {
            val row = cart.getJSONObject(i)
            container.shopping.setQuantity(customer, row.getLong("id"), row.getInt("quantity"))
        }
        container.preferences.setKeepSession(f.getBoolean("keepSession"), DemoAccount.email)
        check(journal.delete())
    }

    private fun save(f: JSONObject) { journal.writeText(f.toString()) }
    private fun read() = JSONObject(journal.readText())
    private fun exists(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
    private fun awaitText(text: String) {
        try {
            compose.waitUntil(15_000) { exists(text) && compose.onNodeWithText(text).isDisplayed() }
        } catch (error: ComposeTimeoutException) {
            screenshot("phase4-failure")
            throw AssertionError("Expected: $text\n" + compose.onRoot().printToString(), error)
        }
    }
    private fun screenshot(name: String) {
        checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()).let { bitmap ->
            File(compose.activity.getExternalFilesDir(null), "$name.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}
