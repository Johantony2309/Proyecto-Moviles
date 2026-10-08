package com.example.proyectopanaderia.data.local.database.dao
import androidx.room.*
import com.example.proyectopanaderia.data.local.database.entity.*
import com.example.proyectopanaderia.data.local.database.relation.OrderWithItems
import com.example.proyectopanaderia.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow
@Dao
interface OrderDao {
    @Transaction @Query("SELECT * FROM orders WHERE customerId = :customerId AND (:status IS NULL OR status = :status) ORDER BY createdAt DESC, id DESC")
    fun orders(customerId: Long, status: OrderStatus?): Flow<List<OrderWithItems>>
    @Transaction @Query("SELECT * FROM orders WHERE customerId = :customerId AND id = :orderId")
    fun order(customerId: Long, orderId: Long): Flow<OrderWithItems?>
    @Transaction @Query("SELECT * FROM orders WHERE customerId = :customerId AND id = :orderId")
    suspend fun orderOnce(customerId: Long, orderId: Long): OrderWithItems?
    @Insert suspend fun insert(value: OrderEntity): Long
    @Insert suspend fun insertItems(values: List<OrderItemEntity>)
    @Query("UPDATE order_items SET quantity = :quantity WHERE id = :itemId AND orderId = :orderId")
    suspend fun updateQuantity(orderId: Long, itemId: Long, quantity: Int): Int
    @Query("DELETE FROM order_items WHERE id = :itemId AND orderId = :orderId")
    suspend fun deleteItem(orderId: Long, itemId: Long): Int
    @Query("UPDATE orders SET status = :status WHERE id = :orderId AND customerId = :customerId")
    suspend fun setStatus(customerId: Long, orderId: Long, status: OrderStatus): Int
    @Query("DELETE FROM orders WHERE id = :orderId AND customerId = :customerId")
    suspend fun delete(customerId: Long, orderId: Long): Int
}
