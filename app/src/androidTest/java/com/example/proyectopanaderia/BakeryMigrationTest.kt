package com.example.proyectopanaderia

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.proyectopanaderia.data.local.database.*
import com.example.proyectopanaderia.data.repository.RoomCatalogRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Isolated, uniquely named test files only; never opens or deletes panaderia.db. */
@RunWith(AndroidJUnit4::class)
class BakeryMigrationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val tables = listOf("categories", "products", "customers", "cart_items", "favorites", "orders", "order_items")

    private fun createExisting(version: Int, name: String) {
        val asset = "com.example.proyectopanaderia.data.local.database.BakeryDatabase/$version.json"
        val schema = InstrumentationRegistry.getInstrumentation().context.assets.open(asset)
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                    override fun onConfigure(db: SupportSQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        val entities = schema.getJSONArray("entities")
                        for (i in 0 until entities.length()) {
                            val entity = entities.getJSONObject(i)
                            val table = entity.getString("tableName")
                            db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                            val indices = entity.getJSONArray("indices")
                            for (j in 0 until indices.length()) {
                                db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
                            }
                        }
                        val setup = schema.getJSONArray("setupQueries")
                        for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
                        db.execSQL("INSERT INTO categories VALUES (101, 'Panes', 99), (102, 'Pasteles', 4), (103, 'Dulces', 5)")
                        db.execSQL("""INSERT INTO products VALUES
                            (201,101,'Croissant','Receta personalizada',777,'foto_propia',0),
                            (202,102,'Torta de chocolate','Mediana · 8 porciones',800,'cake',1),
                            (203,101,'Pan de campo','Masa madre · 500 g',325,'bread',1),
                            (204,103,'Rollo de canela','Canela y glaseado',225,'roll',1),
                            (205,101,'Producto propio','Personalizado',900,'content://foto',1)""")
                        db.execSQL("INSERT INTO customers VALUES (301,'Cliente propio','propio@example.com')")
                        db.execSQL("INSERT INTO cart_items VALUES (301,201,7)")
                        db.execSQL("INSERT INTO favorites VALUES (301,201,1234)")
                        db.execSQL("INSERT INTO orders VALUES (401,301,5678,'ARCHIVED','token-conservado')")
                        db.execSQL("INSERT INTO order_items VALUES (501,401,201,'Nombre histórico',199,2)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                        error("Only Room must perform the tested migration")
                    }
                }).build(),
        )
        helper.writableDatabase
        helper.close()
    }

    private fun open(name: String, seed: Boolean): BakeryDatabase =
        Room.databaseBuilder(context, BakeryDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2)
            .apply { if (seed) addCallback(CatalogSeederCallback()) }
            .build()

    private inline fun <T> BakeryDatabase.withDatabase(block: (BakeryDatabase) -> T): T =
        try { block(this) } finally { close() }

    private fun snapshot(db: SupportSQLiteDatabase) = snapshot { db.query(it) }

    private fun snapshot(query: (String) -> android.database.Cursor) = tables.associateWith { table ->
        query("SELECT * FROM $table ORDER BY rowid").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add((0 until cursor.columnCount).map {
                    if (cursor.isNull(it)) null else cursor.getString(it)
                })
            }
        }
    }

    @Test fun roomValidatesMigrationAndPreservesEveryRow() {
        val name = "migration-${UUID.randomUUID()}.db"
        createExisting(1, name)
        // Read v1 without a Room open, which would already migrate it.
        val raw = context.openOrCreateDatabase(name, 0, null)
        val before = snapshot { raw.rawQuery(it, null) }
        raw.close()
        open(name, seed = false).withDatabase { room ->
            val db = room.openHelper.writableDatabase // Forces Room's generated schema validation.
            assertEquals(2, db.version)
            assertEquals(before, snapshot(db))
            db.query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
        }
    }

    @Test fun existingV1AndV2ReceiveMissingCatalogWithoutDuplicatesOrUserChanges() = runBlocking {
        for (version in listOf(1, 2)) {
            val name = "catalog-$version-${UUID.randomUUID()}.db"
            createExisting(version, name)
            val raw = context.openOrCreateDatabase(name, 0, null)
            val before = snapshot { raw.rawQuery(it, null) }
            raw.close()
            val seeded = open(name, seed = true).withDatabase { room ->
                val db = room.openHelper.writableDatabase
                val after = snapshot(db)
                tables.forEach { assertTrue("Preserve $it", after.getValue(it).containsAll(before.getValue(it))) }
                assertEquals(CatalogSeed.categories.values.sumOf { it.size } + 1, room.catalogDao().productCount())
                RoomCatalogRepository(room).loadExampleCatalog()
                RoomCatalogRepository(room).loadExampleCatalog()
                assertEquals(after, snapshot(db))
                after
            }
            open(name, seed = true).withDatabase { room -> assertEquals(seeded, snapshot(room.openHelper.writableDatabase)) }
        }
    }

    @Test fun newDatabaseLoadsEveryImageAndBothColadaPresentations() = runBlocking {
        Room.inMemoryDatabaseBuilder(context, BakeryDatabase::class.java)
            .addCallback(CatalogSeederCallback()).build().withDatabase { room ->
                val db = room.openHelper.writableDatabase
                val expected = CatalogSeed.categories.values.flatten()
                assertEquals(expected.size, room.catalogDao().productCount())
                db.query("SELECT name, description, imageKey FROM products").use { cursor ->
                    val actual = buildSet {
                        while (cursor.moveToNext()) add(Triple(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
                    }
                    assertEquals(expected.map { Triple(it.name, it.description, it.imageKey) }.toSet(), actual)
                }
                val before = snapshot(db)
                RoomCatalogRepository(room).loadExampleCatalog()
                assertEquals(before, snapshot(db))
            }
    }
}
