package com.example.proyectopanaderia.app

import android.content.Context
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.example.proyectopanaderia.data.local.database.BakeryDatabase
import com.example.proyectopanaderia.data.local.preferences.LocalPreferences
import com.example.proyectopanaderia.data.local.preferences.localDataStore
import com.example.proyectopanaderia.data.repository.*
import com.example.proyectopanaderia.domain.repository.*
import com.example.proyectopanaderia.presentation.login.LoginViewModel
import com.example.proyectopanaderia.presentation.shop.ShopViewModel

/** One container per process; Views never create repositories or database instances. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database by lazy {
        Room.databaseBuilder(appContext, BakeryDatabase::class.java, "panaderia.db").build()
    }
    val preferences: PreferencesRepository by lazy { LocalPreferences(appContext.localDataStore) }
    val catalog: CatalogRepository by lazy { RoomCatalogRepository(database) }
    val customers: CustomerRepository by lazy { RoomCustomerRepository(database.customerDao()) }
    val shopping: ShoppingRepository by lazy { RoomShoppingRepository(database) }
    val orders: OrderRepository by lazy { RoomOrderRepository(database) }
    val loginFactory = viewModelFactory { initializer { LoginViewModel(preferences) } }
    fun shopFactory(email: String) = viewModelFactory { initializer { ShopViewModel(email, customers, catalog, shopping) } }

}
