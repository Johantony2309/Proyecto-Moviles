package com.example.proyectopanaderia

import com.example.proyectopanaderia.domain.repository.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Exposes the app's actual singleton repositories to the USB offline test, in debug only. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface OfflineTestRepositories {
    val customers: CustomerRepository
    val catalog: CatalogRepository
    val shopping: ShoppingRepository
    val orders: OrderRepository
    val preferences: PreferencesRepository
}
