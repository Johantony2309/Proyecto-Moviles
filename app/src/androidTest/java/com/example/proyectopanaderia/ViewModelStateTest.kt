package com.example.proyectopanaderia

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.proyectopanaderia.data.local.database.BakeryDatabase
import com.example.proyectopanaderia.data.repository.*
import com.example.proyectopanaderia.domain.model.*
import com.example.proyectopanaderia.domain.repository.OrderRepository
import com.example.proyectopanaderia.presentation.common.*
import com.example.proyectopanaderia.presentation.cart.CartViewModel
import com.example.proyectopanaderia.presentation.catalog.CatalogViewModel
import com.example.proyectopanaderia.presentation.favorites.FavoritesViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Isolated Room database; never changes the phone's bakery data. */
@RunWith(AndroidJUnit4::class)
class ViewModelStateTest {
    private lateinit var db: BakeryDatabase
    private lateinit var catalog: RoomCatalogRepository
    private lateinit var shopping: RoomShoppingRepository
    private lateinit var customers: RoomCustomerRepository
    private lateinit var orders: RoomOrderRepository
    private val store = ViewModelStore()
    private var nextKey = 0
    private val email = "state-test@panaderia.com"

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext,
            BakeryDatabase::class.java).build()
        catalog = RoomCatalogRepository(db)
        shopping = RoomShoppingRepository(db)
        customers = RoomCustomerRepository(db.customerDao())
        orders = RoomOrderRepository(db)
    }
    @After fun close() {
        runBlocking { withContext(Dispatchers.Main) { store.clear() } }
        db.close()
    }
    private suspend fun <T : ViewModel> model(create: () -> T): T = withContext(Dispatchers.Main) {
        create().also { store.put((nextKey++).toString(), it) }
    }
    private suspend fun <T> ScreenViewModel<T>.content(predicate: (T) -> Boolean = { true }): ScreenState.Content<T> =
        withTimeout(10_000) {
            state.filterIsInstance<ScreenState.Content<T>>().first { !it.busy && predicate(it.data) }
        }

    @Test fun loadingReadFailureRetryAndWriteFailureAreDistinct() = runBlocking {
        val vm = model { FailureModel() }
        assertEquals(ScreenState.Loading, vm.state.value)
        withContext(Dispatchers.Main) { vm.load(fail = true) }
        withTimeout(5_000) { vm.state.first { it is ScreenState.Error } }
        withContext(Dispatchers.Main) { vm.load(fail = false) }
        assertEquals("contenido", vm.content().data)

        // The error is an effect; an unsuccessful write does not discard readable content.
        withContext(Dispatchers.Main) { vm.writeFailure() }
        val event = withTimeout(5_000) { vm.events.first() }
        assertEquals(UiEvent.Message("Operación rechazada"), event)
        assertEquals("contenido", vm.content().data)
        // A new subscriber (e.g. after recreation) must not replay an already consumed notice.
        assertNull(withTimeoutOrNull(150) { vm.events.first() })
    }

    @Test fun independentScreensObserveRoomAndRestoreCatalogSelection() = runBlocking {
        val category = catalog.saveCategory(Category(name = "Panes"))
        val product = catalog.saveProduct(Product(categoryId = category, name = "Pan",
            description = "Prueba", priceCents = 150))
        val handle = SavedStateHandle(mapOf("query" to "Pan", "categoryId" to category))
        val c = model { CatalogViewModel(email, customers, catalog, shopping, handle) }
        val f = model { FavoritesViewModel(email, customers, shopping) }
        val cart = model { CartViewModel(email, customers, shopping, orders) }
        assertEquals("Pan", c.content().data.query)
        assertEquals(category, c.content().data.categoryId)
        f.content()
        cart.content()
        withContext(Dispatchers.Main) { c.favorite(product, true) }
        assertEquals(product, f.content { it.isNotEmpty() }.data.single().id)
        withContext(Dispatchers.Main) { f.add(product) }
        assertEquals(1, cart.content { it.isNotEmpty() }.data.single().quantity)
        assertTrue(withTimeout(5_000) { f.events.first() } is UiEvent.Message)
        withContext(Dispatchers.Main) { f.remove(product) }
        f.content { it.isEmpty() }
        c.content { it.favoriteIds.isEmpty() }
        withContext(Dispatchers.Main) { c.setQuery("No existe") }
        assertTrue(c.content { it.query == "No existe" }.data.products.isEmpty())
        val restored = model {
            CatalogViewModel(email, customers, catalog, shopping,
                SavedStateHandle(mapOf("query" to handle.get<String>("query"),
                    "categoryId" to handle.get<Long>("categoryId"))))
        }
        assertEquals("No existe", restored.content().data.query)
        assertFalse(restored.content().data.catalogEmpty)
    }

    @Test fun repeatedConfirmationCreatesOneOrderAndOneNavigationEvent() = runBlocking {
        val customer = customers.ensureLocalCustomer(email)
        val category = catalog.saveCategory(Category(name = "Panes"))
        val product = catalog.saveProduct(Product(categoryId = category, name = "Pan",
            description = "", priceCents = 100))
        shopping.setQuantity(customer, product, 2)
        val gate = CompletableDeferred<Unit>()
        val delayedOrders = object : OrderRepository by orders {
            override suspend fun createFromCart(customerId: Long): Long {
                gate.await()
                return orders.createFromCart(customerId)
            }
        }
        val vm = model { CartViewModel(email, customers, shopping, delayedOrders) }
        vm.content { it.isNotEmpty() }
        withContext(Dispatchers.Main) { vm.confirm(); vm.confirm() }
        assertTrue((vm.state.value as ScreenState.Content).busy)
        gate.complete(Unit)
        val event = withTimeout(5_000) { vm.events.first() } as UiEvent.OrderCreated
        vm.content { it.isEmpty() }
        assertEquals(event.orderId, orders.orders(customer).first().single().id)
        assertNull(withTimeoutOrNull(150) { vm.events.first() })
        withContext(Dispatchers.Main) { vm.confirm() }
        assertTrue(withTimeout(5_000) { vm.events.first() } is UiEvent.Message)
        assertTrue(vm.content().data.isEmpty())
        assertEquals(1, orders.orders(customer).first().size)
    }

    private class FailureModel : ScreenViewModel<String>() {
        fun load(fail: Boolean) = observe {
            flow { if (fail) error("Read failure") else emit("contenido") }
        }
        fun writeFailure() = change { throw IllegalArgumentException("Operación rechazada") }
    }
}
