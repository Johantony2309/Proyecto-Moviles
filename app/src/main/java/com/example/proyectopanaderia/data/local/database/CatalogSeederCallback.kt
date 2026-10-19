package com.example.proyectopanaderia.data.local.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/** Seeds the demo catalog the first time the database file is created. */
internal class CatalogSeederCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        CatalogSeed.categories.forEach { (categoryName, products) ->
            val categoryValues = ContentValues().apply {
                put("name", categoryName)
                put("position", catalogOrder(categoryName))
            }
            val categoryId = db.insert("categories", SQLiteDatabase.CONFLICT_IGNORE, categoryValues)
            if (categoryId <= 0L) return@forEach

            products.forEach { product ->
                val productValues = ContentValues().apply {
                    put("categoryId", categoryId)
                    put("name", product.name)
                    put("description", product.description)
                    put("priceCents", product.priceCents)
                    put("imageKey", product.imageKey)
                    put("available", 1)
                }
                db.insert("products", SQLiteDatabase.CONFLICT_IGNORE, productValues)
            }
        }
    }

    private fun catalogOrder(categoryName: String): Int =
        CatalogSeed.categories.keys.indexOf(categoryName)
}
