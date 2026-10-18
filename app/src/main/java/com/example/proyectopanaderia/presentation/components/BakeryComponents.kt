package com.example.proyectopanaderia.presentation.components

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
import androidx.compose.ui.res.stringResource
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

@Composable
internal fun ScreenSurface(
    sidePanel: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val wide = currentWindowSizeClass().hasWideLayout
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding), contentAlignment = Alignment.TopCenter) {
            if (wide && sidePanel != null) {
                Row(Modifier.widthIn(max = 1000.dp).fillMaxSize().padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(0.8f).fillMaxHeight().verticalScroll(rememberScrollState()), content = sidePanel)
                    Column(Modifier.weight(1.2f).fillMaxHeight().imePadding().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
                }
            } else {
                Column(
                    modifier = Modifier.widthIn(max = if (wide) 840.dp else 480.dp).fillMaxWidth().fillMaxHeight()
                        .imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    sidePanel?.invoke(this)
                    content()
                }
            }
        }
    }
}

@Composable
internal fun BrandHeader() {
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp)).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(painterResource(R.drawable.ic_bakery), contentDescription = null, modifier = Modifier.size(72.dp))
        Text(stringResource(R.string.app_name), fontFamily = FontFamily.Serif, fontSize = 28.sp,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("HECHO CON CALMA, PARA TI", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
internal fun Heading(text: String, textAlign: TextAlign = TextAlign.Start) {
    Text(text, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp,
        lineHeight = 34.sp, textAlign = textAlign, modifier = Modifier.fillMaxWidth().semantics { heading() })
}

@Composable
internal fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
internal fun Message(message: String?, isError: Boolean = false) {
    if (message != null) Text(message, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}
