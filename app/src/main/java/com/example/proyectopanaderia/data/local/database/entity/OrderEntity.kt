package com.example.proyectopanaderia.data.local.database.entity
import androidx.room.*
import com.example.proyectopanaderia.domain.model.OrderStatus
@Entity(tableName = "orders",
    foreignKeys = [ForeignKey(entity = CustomerEntity::class, parentColumns = ["id"], childColumns = ["customerId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("customerId"), Index(value = ["ticketToken"], unique = true)])
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long, val createdAt: Long, val status: OrderStatus = OrderStatus.SAVED,
    val ticketToken: String,
)
