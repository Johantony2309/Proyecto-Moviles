package com.example.proyectopanaderia.data.local.database.dao
import androidx.room.*
import com.example.proyectopanaderia.data.local.database.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE email = :email LIMIT 1") suspend fun byEmail(email: String): CustomerEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertIfMissing(value: CustomerEntity): Long
    @Query("SELECT * FROM customers ORDER BY name") fun customers(): Flow<List<CustomerEntity>>
    @Insert suspend fun insert(value: CustomerEntity): Long
    @Update suspend fun update(value: CustomerEntity): Int
    @Query("DELETE FROM customers WHERE id = :id") suspend fun delete(id: Long): Int
}
