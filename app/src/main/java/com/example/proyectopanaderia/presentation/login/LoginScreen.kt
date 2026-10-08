package com.example.proyectopanaderia.presentation.login

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
import com.example.proyectopanaderia.presentation.login.LoginState
import com.example.proyectopanaderia.presentation.login.LoginViewModel
import com.example.proyectopanaderia.R
import com.example.proyectopanaderia.ui.theme.ProyectoPanaderiaTheme

import com.example.proyectopanaderia.presentation.components.*

@Composable
internal fun ColumnScope.LoginScreen(
    state: LoginState,
    signIn: (String, String) -> Unit,
    clearMessage: () -> Unit,
    openSettings: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf(state.settings.savedEmail) }
    // Password stays only in memory; never save it to a Bundle or DataStore.
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val emailError = if (submitted) DemoAccount.emailError(email) else null
    val passwordError = if (submitted && password.isBlank()) "Escribe tu contraseña." else null
    val submit: () -> Unit = {
        submitted = true
        if (DemoAccount.emailError(email) != null) emailFocus.requestFocus()
        else if (password.isBlank()) passwordFocus.requestFocus()
        else {
            focusManager.clearFocus()
            if (!state.busy) signIn(email, password)
        }
    }
    BrandHeader()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Heading("¡Qué gusto verte!")
        Text("Entra y siéntete como en casa.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    OutlinedTextField(
        value = email, onValueChange = { email = it; clearMessage() },
        label = { Text("Correo electrónico") }, placeholder = { Text("nombre@correo.com") },
        enabled = !state.busy, singleLine = true, isError = emailError != null,
        supportingText = emailError?.let { error -> { Text(error) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
        modifier = Modifier.fillMaxWidth().focusRequester(emailFocus), shape = RoundedCornerShape(12.dp),
    )
    OutlinedTextField(
        value = password, onValueChange = { password = it; clearMessage() },
        label = { Text("Contraseña") }, enabled = !state.busy, singleLine = true,
        isError = passwordError != null,
        supportingText = passwordError?.let { error -> { Text(error) } },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = { passwordVisible = !passwordVisible }, enabled = !state.busy) {
                Text(if (passwordVisible) "Ocultar" else "Mostrar")
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { submit() }),
        modifier = Modifier.fillMaxWidth().focusRequester(passwordFocus), shape = RoundedCornerShape(12.dp),
    )
    Message(state.message, isError = true)
    Button(onClick = submit, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(12.dp)) {
        if (state.busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        else Text("Iniciar sesión", fontWeight = FontWeight.Bold)
    }
    InfoCard {
        Text("Prueba la experiencia", fontWeight = FontWeight.Bold)
        Text("Cuenta de demostración · sin conexión", style = MaterialTheme.typography.bodySmall)
        Text("Correo: ${DemoAccount.email}", style = MaterialTheme.typography.bodyMedium)
        Text("Contraseña: ${DemoAccount.password}", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = {
            email = DemoAccount.email
            password = DemoAccount.password
            submitted = false
            clearMessage()
        }, enabled = !state.busy) { Text("Usar datos de demostración") }
    }
    TextButton(onClick = openSettings, enabled = !state.busy, modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text("Configuración de este dispositivo")
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreview() {
    ProyectoPanaderiaTheme { ScreenSurface { LoginScreen(LoginState(loading = false), { _, _ -> }, {}, {}) } }
}
