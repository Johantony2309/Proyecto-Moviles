package com.example.proyectopanaderia.app
import android.app.Application

class BakeryApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
