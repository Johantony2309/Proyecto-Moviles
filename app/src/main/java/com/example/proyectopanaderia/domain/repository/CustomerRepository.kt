package com.example.proyectopanaderia.domain.repository
import com.example.proyectopanaderia.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun ensureLocalCustomer(email: String): Long
    fun customers(): Flow<List<Customer>>
    suspend fun save(customer: Customer): Long
    suspend fun delete(id: Long)
}
