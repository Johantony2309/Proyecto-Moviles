package com.example.proyectopanaderia.data.repository
import com.example.proyectopanaderia.data.local.database.dao.CustomerDao
import com.example.proyectopanaderia.data.local.database.entity.CustomerEntity
import com.example.proyectopanaderia.domain.model.Customer
import com.example.proyectopanaderia.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.map
import java.util.Locale

class RoomCustomerRepository(private val dao: CustomerDao) : CustomerRepository {
    override suspend fun ensureLocalCustomer(email: String): Long {
        val normalized = email.trim().lowercase(Locale.ROOT)
        dao.byEmail(normalized)?.let { return it.id }
        dao.insertIfMissing(CustomerEntity(name = "Cliente de demostración", email = normalized))
        return checkNotNull(dao.byEmail(normalized)).id
    }
    override fun customers() = dao.customers().map { list -> list.map { it.toModel() } }
    override suspend fun save(customer: Customer): Long {
        require(customer.name.isNotBlank()) { "El cliente necesita un nombre." }
        val email = customer.email.trim().lowercase(Locale.ROOT)
        require(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email)) { "Revisa el correo del cliente." }
        val entity = CustomerEntity(customer.id, customer.name.trim(), email)
        if (entity.id == 0L) return dao.insert(entity)
        check(dao.update(entity) == 1) { "El cliente ya no existe." }
        return entity.id
    }
    override suspend fun delete(id: Long) {
        check(dao.delete(id) == 1) { "El cliente ya no existe." }
    }
}
