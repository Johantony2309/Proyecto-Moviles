package com.example.proyectopanaderia

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.proyectopanaderia.data.local.database.BakeryDatabase
import com.example.proyectopanaderia.data.repository.*
import com.example.proyectopanaderia.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** These tests use an isolated database, never the user's panaderia.db. */
@RunWith(AndroidJUnit4::class)
class RoomWorkflowTest {
    private lateinit var db: BakeryDatabase
    private lateinit var catalog: RoomCatalogRepository
    private lateinit var customers: RoomCustomerRepository
    private lateinit var shopping: RoomShoppingRepository
    private lateinit var orders: RoomOrderRepository
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, BakeryDatabase::class.java).build()
        catalog = RoomCatalogRepository(db)
        customers = RoomCustomerRepository(db.customerDao())
        shopping = RoomShoppingRepository(db)
        orders = RoomOrderRepository(db)
    }
    @After fun close() { db.close() }

    @Test fun orderCrudPreservesHistoricalPriceAndClearsCart() = runBlocking {
        val customer = customers.ensureLocalCustomer("demo@panaderia.com")
        val category = catalog.saveCategory(Category(name = "Panes"))
        val product = catalog.saveProduct(Product(categoryId = category, name = "Croissant", description = "", priceCents = 150))
        shopping.changeQuantity(customer, product, 1)
        shopping.changeQuantity(customer, product, 1)
        val orderId = orders.createFromCart(customer)
        assertTrue(shopping.cart(customer).first().isEmpty())
        val original = orders.order(customer, orderId).first()!!
        assertEquals(300L, original.totalCents)
        catalog.saveProduct(Product(product, category, "Croissant nuevo", "", 999))
        orders.updateItemQuantity(customer, orderId, original.items.single().id, 3)
        val edited = orders.order(customer, orderId).first()!!
        assertEquals(450L, edited.totalCents)
        assertEquals("Croissant", edited.items.single().productName)
        orders.setArchived(customer, orderId, true)
        assertEquals(OrderStatus.ARCHIVED, orders.order(customer, orderId).first()!!.status)
        orders.delete(customer, orderId)
        assertNull(orders.order(customer, orderId).first())
    }
    @Test fun repetitionRollsBackIfOneProductIsUnavailable() = runBlocking {
        val customer = customers.ensureLocalCustomer("demo@panaderia.com")
        val category = catalog.saveCategory(Category(name = "Panes"))
        val a = catalog.saveProduct(Product(categoryId = category, name = "A", description = "", priceCents = 150))
        val b = catalog.saveProduct(Product(categoryId = category, name = "B", description = "", priceCents = 200))
        shopping.setQuantity(customer, a, 1); shopping.setQuantity(customer, b, 1)
        val orderId = orders.createFromCart(customer)
        catalog.saveProduct(Product(b, category, "B", "", 200, available = false))
        try { orders.repeatToCart(customer, orderId); fail("Must reject unavailable products") }
        catch (_: IllegalArgumentException) { }
        assertTrue(shopping.cart(customer).first().isEmpty())
    }
    @Test fun deletingProductPreservesOrderAndClearsFavorites() = runBlocking {
        val customer = customers.ensureLocalCustomer("demo@panaderia.com")
        val category = catalog.saveCategory(Category(name = "Panes"))
        val product = catalog.saveProduct(Product(categoryId = category, name = "Pan", description = "", priceCents = 100))
        shopping.setFavorite(customer, product, true)
        shopping.setQuantity(customer, product, 1)
        val orderId = orders.createFromCart(customer)
        catalog.deleteProduct(product)
        assertTrue(shopping.favorites(customer).first().isEmpty())
        val order = orders.order(customer, orderId).first()!!
        assertNull(order.items.single().productId)
        assertEquals(100L, order.totalCents)
    }
    @Test fun bootstrapDoesNotDuplicateCustomerOrExampleProducts() = runBlocking {
        val first = customers.ensureLocalCustomer("demo@panaderia.com")
        assertEquals(first, customers.ensureLocalCustomer("DEMO@PANADERIA.COM"))
        catalog.loadExampleCatalog()
        try { catalog.loadExampleCatalog(); fail("Must not duplicate examples") }
        catch (_: IllegalArgumentException) { }
        assertEquals(4, catalog.products().first().size)
        assertEquals(3, catalog.categories().first().size)
    }
}
