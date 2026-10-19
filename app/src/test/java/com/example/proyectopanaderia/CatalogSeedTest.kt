package com.example.proyectopanaderia

import com.example.proyectopanaderia.data.local.database.CatalogSeed
import org.junit.Assert.*
import org.junit.Test

class CatalogSeedTest {
    @Test fun exportedSchemasOnlyDifferInDatabaseVersion() {
        val path = "com.example.proyectopanaderia.data.local.database.BakeryDatabase"
        fun schema(version: Int) = checkNotNull(javaClass.classLoader?.getResource("$path/$version.json"))
            .readText()
        // Full comparison includes entities, indices, foreign keys, identity hash and setup queries.
        assertEquals(schema(1).replace("\"version\": 1", "\"version\": 2"), schema(2))
    }

    @Test fun catalogHasUniqueIdentitiesAndNonEmptyImages() {
        val identities = CatalogSeed.categories.flatMap { (category, products) ->
            products.map { product ->
                assertTrue(product.imageKey.isNotBlank())
                assertTrue(product.priceCents > 0)
                Triple(category, product.name, CatalogSeed.variantDescription(category, product))
            }
        }
        assertEquals(identities.size, identities.toSet().size)
    }

    @Test fun coladaPresentationsAreDistinctButUniqueNamesRespectCustomization() {
        val coladas = CatalogSeed.categories.getValue("Bebidas").filter { it.name == "Colada Morada" }
        assertEquals(setOf("1 litro", "1/2 litro"), coladas.map {
            CatalogSeed.variantDescription("Bebidas", it)
        }.toSet())
        assertEquals(2, coladas.map { it.imageKey }.toSet().size)
        val custom = CatalogSeed.categories.getValue("Panes").first().copy(description = "Receta propia")
        assertNull(CatalogSeed.variantDescription("Panes", custom))
    }
}
