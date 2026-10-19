package com.example.proyectopanaderia.data.local.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/** Also fills missing demo entries in existing v1 (migrated) and v2 installations. */
internal class CatalogSeederCallback : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            CatalogSeedStore.insertMissing(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
