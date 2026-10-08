package com.example.proyectopanaderia.data.local.database.entity
import androidx.room.*
@Entity(tableName = "products",
    foreignKeys = [ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("categoryId")])
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long, val name: String, val description: String, val priceCents: Long,
    val imageKey: String? = null, val available: Boolean = true,
)
