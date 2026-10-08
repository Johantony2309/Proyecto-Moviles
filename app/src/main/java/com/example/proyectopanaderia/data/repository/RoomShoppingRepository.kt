package com.example.proyectopanaderia.data.repository
import androidx.room.withTransaction
import com.example.proyectopanaderia.data.local.database.BakeryDatabase
import com.example.proyectopanaderia.data.local.database.entity.*
import com.example.proyectopanaderia.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.map

class RoomShoppingRepository(private val db: BakeryDatabase) : ShoppingRepository {
    private val dao = db.shoppingDao()
    override suspend fun changeQuantity(customerId: Long, productId: Long, delta: Int) = db.withTransaction {
        val current = dao.cartOnce(customerId).firstOrNull { it.item.productId == productId }?.item?.quantity ?: 0
        setQuantity(customerId, productId, Math.addExact(current, delta))
    }
    override fun cart(customerId: Long) = dao.cart(customerId).map { list -> list.map { it.toModel() } }
    override fun favorites(customerId: Long) = dao.favorites(customerId).map { list -> list.map { it.toModel() } }
    override suspend fun setQuantity(customerId: Long, productId: Long, quantity: Int) {
        require(quantity in 0..999) { "La cantidad debe estar entre 0 y 999." }
        db.withTransaction {
            if (quantity == 0) dao.removeCartItem(customerId, productId)
            else {
                val product = requireNotNull(db.catalogDao().product(productId)) { "El producto ya no existe." }
                require(product.available) { "El producto no está disponible." }
                dao.putCartItem(CartItemEntity(customerId, productId, quantity))
            }
        }
    }
    override suspend fun clearCart(customerId: Long) = dao.clearCart(customerId)
    override suspend fun setFavorite(customerId: Long, productId: Long, favorite: Boolean) {
        if (favorite) dao.addFavorite(FavoriteEntity(customerId, productId, System.currentTimeMillis()))
        else dao.removeFavorite(customerId, productId)
    }
}
