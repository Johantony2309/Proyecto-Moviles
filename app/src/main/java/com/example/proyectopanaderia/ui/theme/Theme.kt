package com.example.proyectopanaderia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF9F4229),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E5CD),
    onPrimaryContainer = Color(0xFF513B29),
    secondary = Color(0xFF71543B),
    background = Color(0xFFFFFBF4),
    onBackground = Color(0xFF35281F),
    surface = Color(0xFFFFFBF4),
    onSurface = Color(0xFF35281F),
    surfaceVariant = Color(0xFFF3EBDD),
    onSurfaceVariant = Color(0xFF6B594B),
    outline = Color(0xFF8E7C69),
    outlineVariant = Color(0xFFDFD2BE),
    error = Color(0xFFB3261E),
)

@Composable
fun ProyectoPanaderiaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
