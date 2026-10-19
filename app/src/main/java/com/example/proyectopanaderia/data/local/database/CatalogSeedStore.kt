package com.example.proyectopanaderia.data.local.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/** Insert-only bootstrap. Call within a transaction; never infer ownership from an ID. */
internal object CatalogSeedStore {
    fun insertMissing(db: SupportSQLiteDatabase) {
        CatalogSeed.categories.entries.forEachIndexed { position, (categoryName, products) ->
            val categoryId = db.query("SELECT id FROM categories WHERE name = ?", arrayOf(categoryName)).use {
                if (it.moveToFirst()) it.getLong(0) else null
            } ?: db.insert("categories", SQLiteDatabase.CONFLICT_ABORT, ContentValues().apply {
                put("name", categoryName)
                put("position", position)
            })

            products.forEach { product ->
                // Names with multiple presentations need their description to distinguish them.
                val description = CatalogSeed.variantDescription(categoryName, product)
                val exists = db.query(
                    "SELECT 1 FROM products WHERE categoryId = ? AND name = ?" +
                        if (description == null) " LIMIT 1" else " AND description = ? LIMIT 1",
                    if (description == null) arrayOf<Any>(categoryId, product.name)
                    else arrayOf<Any>(categoryId, product.name, description),
                ).use { it.moveToFirst() }
                if (!exists) db.insert("products", SQLiteDatabase.CONFLICT_ABORT, ContentValues().apply {
                    put("categoryId", categoryId)
                    put("name", product.name)
                    put("description", product.description)
                    put("priceCents", product.priceCents)
                    put("imageKey", product.imageKey)
                    put("available", 1)
                })
                // Existing rows (including custom prices, descriptions, images or availability) stay untouched.
            }
        }
    }
}
