package com.example.proyectopanaderia.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Versions 1 and 2 have identical exported entities, indices and foreign keys. */
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No DDL or data changes: Room validates the existing schema after this step.
    }
}
