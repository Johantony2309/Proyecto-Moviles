package com.example.proyectopanaderia

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.viewModels
import com.example.proyectopanaderia.app.BakeryApp
import dagger.hilt.android.AndroidEntryPoint
import com.example.proyectopanaderia.presentation.login.LoginViewModel
import com.example.proyectopanaderia.ui.theme.ProyectoPanaderiaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { model.state.value.loading }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent { ProyectoPanaderiaTheme { BakeryApp() } }
    }
}
