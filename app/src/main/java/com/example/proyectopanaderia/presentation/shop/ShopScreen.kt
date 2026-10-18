package com.example.proyectopanaderia.presentation.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.presentation.common.ScreenState
import com.example.proyectopanaderia.R
import com.example.proyectopanaderia.app.AppDestinations
import com.example.proyectopanaderia.presentation.components.currentWindowSizeClass
import com.example.proyectopanaderia.presentation.components.hasWideLayout
import com.example.proyectopanaderia.presentation.components.hasCompactHeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopContent(state: ScreenState<ShopCounts>, retry: () -> Unit, destination: String, navigate: (String) -> Unit,
    settings: () -> Unit, signOut: () -> Unit, sessionBusy: Boolean,
    content: @Composable (Modifier) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val counts = (state as? ScreenState.Content)?.data
    val windowSize = currentWindowSizeClass()
    val sideNavigation = windowSize.hasWideLayout
    val headerHeight = if (windowSize.hasCompactHeight) 80.dp else if (sideNavigation) 120.dp else 104.dp
    val logoSize = if (windowSize.hasCompactHeight) 48.dp else if (sideNavigation) 88.dp else 64.dp
    val tabs = listOf(
        ShopTab(AppDestinations.CATALOG, "Catálogo", Icons.Filled.Home),
        ShopTab(AppDestinations.FAVORITES, "Favoritos", Icons.Filled.Favorite, counts?.favorites ?: 0),
        ShopTab(AppDestinations.CART, "Carrito", Icons.Filled.ShoppingCart, counts?.cart ?: 0),
        ShopTab(AppDestinations.ORDERS, "Pedidos", Icons.AutoMirrored.Filled.List),
    )
    val selectedRoute = if (destination == AppDestinations.TICKET) AppDestinations.ORDERS else destination
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
            if (!sideNavigation) NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedRoute == tab.route,
                        onClick = { navigate(tab.route) }, enabled = !sessionBusy,
                        icon = { ShopTabIcon(tab) }, label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
        Row(modifier) {
            if (sideNavigation) NavigationRail(
                modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState()),
                windowInsets = WindowInsets(0),
            ) {
                tabs.forEach { tab ->
                    NavigationRailItem(
                        selected = selectedRoute == tab.route,
                        onClick = { navigate(tab.route) }, enabled = !sessionBusy,
                        icon = { ShopTabIcon(tab) }, label = { Text(tab.label) },
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                if (state is ScreenState.Error) TextButton(onClick = retry) { Text("Reintentar contadores") }
                content(Modifier.fillMaxWidth().weight(1f))
            }
        }
    }
}

private data class ShopTab(val route: String, val label: String, val icon: ImageVector, val count: Int = 0)

@Composable
private fun ShopTabIcon(tab: ShopTab) {
    BadgedBox(badge = { if (tab.count > 0) Badge { Text(tab.count.toString()) } }) {
        Icon(tab.icon, contentDescription = null)
    }
}
