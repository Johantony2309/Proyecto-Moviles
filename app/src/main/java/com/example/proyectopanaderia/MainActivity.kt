package com.example.proyectopanaderia

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.proyectopanaderia.app.BakeryApp
import com.example.proyectopanaderia.app.BakeryApplication
import com.example.proyectopanaderia.presentation.login.LoginViewModel
import com.example.proyectopanaderia.ui.theme.ProyectoPanaderiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        val container = (application as BakeryApplication).container
        val model = ViewModelProvider(this, container.loginFactory)[LoginViewModel::class.java]
        splash.setKeepOnScreenCondition { model.state.value.loading }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent { ProyectoPanaderiaTheme { BakeryApp(model, container) } }
    }
}
