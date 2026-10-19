package com.example.proyectopanaderia.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.proyectopanaderia.data.local.database.BakeryDatabase
import com.example.proyectopanaderia.data.local.database.CatalogSeederCallback
import com.example.proyectopanaderia.data.local.database.dao.CustomerDao
import com.example.proyectopanaderia.data.local.preferences.LocalPreferences
import com.example.proyectopanaderia.data.local.preferences.localDataStore
import com.example.proyectopanaderia.data.repository.*
import com.example.proyectopanaderia.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): BakeryDatabase =
        Room.databaseBuilder(context, BakeryDatabase::class.java, "panaderia.db")
            .addCallback(CatalogSeederCallback())
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun customerDao(database: BakeryDatabase): CustomerDao = database.customerDao()

    @Provides
    @Singleton
    fun preferencesStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.localDataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun preferences(repository: LocalPreferences): PreferencesRepository

    @Binds @Singleton
    abstract fun catalog(repository: RoomCatalogRepository): CatalogRepository

    @Binds @Singleton
    abstract fun customers(repository: RoomCustomerRepository): CustomerRepository

    @Binds @Singleton
    abstract fun shopping(repository: RoomShoppingRepository): ShoppingRepository

    @Binds @Singleton
    abstract fun orders(repository: RoomOrderRepository): OrderRepository
}
