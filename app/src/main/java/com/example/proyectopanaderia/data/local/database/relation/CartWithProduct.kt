package com.example.proyectopanaderia.data.local.database.relation
import androidx.room.*
import com.example.proyectopanaderia.data.local.database.entity.*
data class CartWithProduct(
    @Embedded val item: CartItemEntity,
    @Relation(parentColumn = "productId", entityColumn = "id") val product: ProductEntity,
)
