package com.example.proyectopanaderia.data.local.database.dao
import androidx.room.*
import com.example.proyectopanaderia.data.local.database.entity.*
import com.example.proyectopanaderia.data.local.database.relation.CartWithProduct
import kotlinx.coroutines.flow.Flow
@Dao
interface ShoppingDao {
    @Transaction @Query("SELECT * FROM cart_items WHERE customerId = :customerId ORDER BY productId")
    fun cart(customerId: Long): Flow<List<CartWithProduct>>
    @Transaction @Query("SELECT * FROM cart_items WHERE customerId = :customerId ORDER BY productId")
    suspend fun cartOnce(customerId: Long): List<CartWithProduct>
    @Query("SELECT p.* FROM products p INNER JOIN favorites f ON p.id = f.productId WHERE f.customerId = :customerId ORDER BY f.createdAt DESC, p.id")
    fun favorites(customerId: Long): Flow<List<ProductEntity>>
    @Upsert suspend fun putCartItem(value: CartItemEntity)
    @Query("DELETE FROM cart_items WHERE customerId = :customerId AND productId = :productId")
    suspend fun removeCartItem(customerId: Long, productId: Long)
    @Query("DELETE FROM cart_items WHERE customerId = :customerId") suspend fun clearCart(customerId: Long)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addFavorite(value: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE customerId = :customerId AND productId = :productId")
    suspend fun removeFavorite(customerId: Long, productId: Long)
}
