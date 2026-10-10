package com.example.proyectopanaderia.presentation.shop

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
            TopAppBar(title = { Text("Panadería", fontFamily = FontFamily.Serif) }, actions = {
                Box {
                    TextButton(onClick = { menu = true }) { Text("Mi cuenta") }
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem(text = { Text("Configuración") }, onClick = { menu = false; settings() }, enabled = !state.busy)
                        DropdownMenuItem(text = { Text("Cerrar sesión") }, onClick = { menu = false; signOut() }, enabled = !state.busy && !sessionBusy)
                    }
                }
            })
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { section = "catalog" }, enabled = !state.busy) { Text("Catálogo") }
                        TextButton(onClick = { section = "favorites" }, enabled = !state.busy) { Text("Favoritos") }
                        TextButton(onClick = { section = "cart" }, enabled = !state.busy) { Text("Carrito") }
                        TextButton(onClick = { section = "orders" }, enabled = !state.busy) { Text("Pedidos") }
                    }
                    Text("Guardados: ${state.data.favorites.size} · En carrito: ${state.data.cart.sumOf { it.quantity }}",
                        style = MaterialTheme.typography.bodySmall)
                }
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
