package com.example.proyectopanaderia.presentation.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.presentation.common.ScreenState
import com.example.proyectopanaderia.R
import com.example.proyectopanaderia.app.AppDestinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopContent(state: ScreenState<ShopCounts>, retry: () -> Unit, destination: String, navigate: (String) -> Unit,
    settings: () -> Unit, signOut: () -> Unit, sessionBusy: Boolean,
    content: @Composable (Modifier) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val counts = (state as? ScreenState.Content)?.data
    val windowHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
    val compactHeight = windowHeight < 480.dp
    val headerHeight = if (compactHeight) 80.dp else if (windowHeight < 700.dp) 104.dp else 120.dp
    val logoSize = if (compactHeight) 48.dp else if (windowHeight < 700.dp) 64.dp else 88.dp
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                expandedHeight = headerHeight,
                title = {
                    Image(
                        painter = painterResource(R.drawable.ic_bakery),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(logoSize),
                    )
                }, actions = {
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Mi cuenta")
                    }
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem(text = { Text("Configuración") }, onClick = { menu = false; settings() }, enabled = !sessionBusy)
                        DropdownMenuItem(text = { Text("Cerrar sesión") }, onClick = { menu = false; signOut() }, enabled = !sessionBusy)
                    }
                }
            })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = destination == AppDestinations.CATALOG,
                    onClick = { navigate(AppDestinations.CATALOG) },
                    enabled = !sessionBusy,
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Catálogo") },
                )
                NavigationBarItem(
                    selected = destination == AppDestinations.FAVORITES,
                    onClick = { navigate(AppDestinations.FAVORITES) },
                    enabled = !sessionBusy,
                    icon = {
                        BadgedBox(badge = {
                            val count = counts?.favorites ?: 0
                            if (count > 0) Badge { Text(count.toString()) }
                        }) { Icon(Icons.Filled.Favorite, contentDescription = null) }
                    },
                    label = { Text("Favoritos") },
                )
                NavigationBarItem(
                    selected = destination == AppDestinations.CART,
                    onClick = { navigate(AppDestinations.CART) },
                    enabled = !sessionBusy,
                    icon = {
                        BadgedBox(badge = {
                            val count = counts?.cart ?: 0
                            if (count > 0) Badge { Text(count.toString()) }
                        }) { Icon(Icons.Filled.ShoppingCart, contentDescription = null) }
                    },
                    label = { Text("Carrito") },
                )
                NavigationBarItem(
                    selected = destination == AppDestinations.ORDERS || destination == AppDestinations.TICKET,
                    onClick = { navigate(AppDestinations.ORDERS) },
                    enabled = !sessionBusy,
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Pedidos") },
                )
            }
        },
    ) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
        Column(modifier) {
            if (state is ScreenState.Error) TextButton(onClick = retry) { Text("Reintentar contadores") }
            content(Modifier.fillMaxWidth().weight(1f))
        }
    }
}
