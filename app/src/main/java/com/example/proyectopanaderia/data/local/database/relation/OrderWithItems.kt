package com.example.proyectopanaderia.data.local.database.relation
import androidx.room.*
import com.example.proyectopanaderia.data.local.database.entity.*
data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "orderId") val items: List<OrderItemEntity>,
)
