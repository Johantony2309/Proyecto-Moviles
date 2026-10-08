package com.example.proyectopanaderia.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopanaderia.domain.auth.DemoAccount
import com.example.proyectopanaderia.domain.model.LocalSettings
import com.example.proyectopanaderia.presentation.login.LoginViewModel
import com.example.proyectopanaderia.R
import com.example.proyectopanaderia.ui.theme.ProyectoPanaderiaTheme

import com.example.proyectopanaderia.presentation.components.*

@Composable
internal fun SettingsScreen(
    settings: LocalSettings,
    busy: Boolean,
    message: String?,
    rememberEmail: (Boolean) -> Unit,
    keepSession: (Boolean) -> Unit,
    back: () -> Unit,
) {
    TextButton(onClick = back, enabled = !busy) { Text("‹ Volver") }
    Heading("A tu manera")
    Text("Configuración", style = MaterialTheme.typography.titleMedium)
    Text("Elige qué recordar en este dispositivo. Los cambios se guardan automáticamente.",
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    PreferenceRow("Recordar mi correo", "Completa tu correo la próxima vez que inicies sesión.",
        settings.rememberEmail, !busy, rememberEmail)
    PreferenceRow("Mantener mi sesión", "Entra sin escribir tus datos al volver a abrir la app. Actívalo solo en tu dispositivo personal.",
        settings.keepSession, !busy, keepSession)
    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    Message(message)
    InfoCard {
        Text("Tus preferencias se quedan aquí", fontWeight = FontWeight.Bold)
        Text("Se guardan únicamente en este dispositivo. La contraseña no se almacena. Al desactivar una opción, se elimina el dato que esa opción recuerda.",
            style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PreferenceRow(title: String, description: String, checked: Boolean, enabled: Boolean, change: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = change)
            .padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsPreview() {
    ProyectoPanaderiaTheme { ScreenSurface { SettingsScreen(LocalSettings(), false, null, {}, {}, {}) } }
}
