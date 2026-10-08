package com.example.proyectopanaderia.data.local.database.entity
import androidx.room.*
@Entity(tableName = "order_items",
    foreignKeys = [
        ForeignKey(entity = OrderEntity::class, parentColumns = ["id"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.SET_NULL),
    ], indices = [Index("orderId"), Index("productId")])
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long, val productId: Long?, val productName: String,
    val unitPriceCents: Long, val quantity: Int,
)
