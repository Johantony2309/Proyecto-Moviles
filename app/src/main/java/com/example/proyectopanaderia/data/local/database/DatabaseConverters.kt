package com.example.proyectopanaderia.data.local.database
import androidx.room.TypeConverter
import com.example.proyectopanaderia.domain.model.OrderStatus
class DatabaseConverters {
    @TypeConverter fun status(value: String): OrderStatus = OrderStatus.valueOf(value)
    @TypeConverter fun status(value: OrderStatus): String = value.name
}
