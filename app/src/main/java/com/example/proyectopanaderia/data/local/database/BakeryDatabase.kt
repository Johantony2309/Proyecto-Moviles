package com.example.proyectopanaderia.data.local.database
import androidx.room.*
import com.example.proyectopanaderia.data.local.database.dao.*
import com.example.proyectopanaderia.data.local.database.entity.*

@Database(entities = [CategoryEntity::class, ProductEntity::class, CustomerEntity::class,
    CartItemEntity::class, FavoriteEntity::class, OrderEntity::class, OrderItemEntity::class],
    version = 1, exportSchema = true)
@TypeConverters(DatabaseConverters::class)
abstract class BakeryDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun customerDao(): CustomerDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun orderDao(): OrderDao
}
