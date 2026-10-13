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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopanaderia.R
import com.example.proyectopanaderia.app.AppContainer
import com.example.proyectopanaderia.presentation.cart.CartScreen
import com.example.proyectopanaderia.presentation.catalog.CatalogScreen
import com.example.proyectopanaderia.presentation.components.EmptyState
import com.example.proyectopanaderia.presentation.favorites.FavoritesScreen
import com.example.proyectopanaderia.presentation.orders.OrdersScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(email: String, container: AppContainer, settings: () -> Unit, signOut: () -> Unit,
    sessionBusy: Boolean, sessionMessage: String?, clearSessionMessage: () -> Unit) {
    val model: ShopViewModel = viewModel(key = "shop:$email", factory = container.shopFactory(email))
    val state by model.state.collectAsStateWithLifecycle()
    var menu by remember { mutableStateOf(false) }
    var section by rememberSaveable { mutableStateOf("catalog") }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(sessionMessage) {
        sessionMessage?.let { snackbar.showSnackbar(it); clearSessionMessage() }
    }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); model.clearMessage() }
    }
    LaunchedEffect(state.createdOrderId) {
        if (state.createdOrderId != null) {
            model.clearCreatedOrder()
            section = "orders"
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.height(148.dp),
                title = {
                    Image(
                        painter = painterResource(R.drawable.ic_bakery),
                        contentDescription = "Panadería",
                        modifier = Modifier.size(120.dp),
                    )
                }, actions = {
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Mi cuenta")
                    }
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem(text = { Text("Configuración") }, onClick = { menu = false; settings() }, enabled = !state.busy)
                        DropdownMenuItem(text = { Text("Cerrar sesión") }, onClick = { menu = false; signOut() }, enabled = !state.busy && !sessionBusy)
                    }
                }
            })
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = section == "catalog",
                    onClick = { section = "catalog" },
                    enabled = !state.busy,
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Catálogo") },
                    label = { Text("Catálogo") },
                )
                NavigationBarItem(
                    selected = section == "favorites",
                    onClick = { section = "favorites" },
                    enabled = !state.busy,
                    icon = {
                        BadgedBox(badge = {
                            val count = state.data.favorites.size
                            if (count > 0) Badge { Text(count.toString()) }
                        }) { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") }
                    },
                    label = { Text("Favoritos") },
                )
                NavigationBarItem(
                    selected = section == "cart",
                    onClick = { section = "cart" },
                    enabled = !state.busy,
                    icon = {
                        BadgedBox(badge = {
                            val count = state.data.cart.sumOf { it.quantity }
                            if (count > 0) Badge { Text(count.toString()) }
                        }) { Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito") }
                    },
                    label = { Text("Carrito") },
                )
                NavigationBarItem(
                    selected = section == "orders",
                    onClick = { section = "orders" },
                    enabled = !state.busy,
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Pedidos") },
                    label = { Text("Pedidos") },
                )
            }
        },
    ) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
        when {
            state.loading -> Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.loadFailed -> Column(modifier.padding(24.dp)) {
                EmptyState("No pudimos abrir la panadería", "Tus datos guardados no se han eliminado.")
                Button(onClick = model::load) { Text("Reintentar") }
            }
            section == "favorites" -> FavoritesScreen(
                state.data.favorites,
                state.busy,
                model::add,
                { productId -> model.favorite(productId, false) },
                modifier,
            )
            section == "cart" -> CartScreen(
                state.data.cart,
                state.busy,
                model::add,
                model::setQuantity,
                model::confirmOrder,
                modifier,
            )
            section == "orders" -> OrdersScreen(email, container, modifier)
            else -> CatalogScreen(state.data.products, state.data.categories,
                state.data.favorites.map { it.id }.toSet(), state.busy,
                model::add, model::favorite, model::examples, modifier)
        }
    }
}
