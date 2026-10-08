package com.example.proyectopanaderia.data.local.database.entity
import androidx.room.*
@Entity(tableName = "customers", indices = [Index(value = ["email"], unique = true)])
data class CustomerEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val email: String)
