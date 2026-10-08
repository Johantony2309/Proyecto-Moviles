package com.example.proyectopanaderia.data.local.database.entity
import androidx.room.*
@Entity(tableName = "cart_items", primaryKeys = ["customerId", "productId"],
    foreignKeys = [
        ForeignKey(entity = CustomerEntity::class, parentColumns = ["id"], childColumns = ["customerId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.CASCADE),
    ], indices = [Index("productId")])
data class CartItemEntity(val customerId: Long, val productId: Long, val quantity: Int)
