package com.example.proyectopanaderia.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.example.proyectopanaderia.presentation.components.*
import com.example.proyectopanaderia.presentation.login.*
import com.example.proyectopanaderia.presentation.settings.SettingsRoute
import com.example.proyectopanaderia.presentation.common.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.launch
import com.example.proyectopanaderia.presentation.shop.*
import com.example.proyectopanaderia.presentation.catalog.CatalogRoute
import com.example.proyectopanaderia.presentation.cart.CartRoute
import com.example.proyectopanaderia.presentation.favorites.FavoritesRoute
import com.example.proyectopanaderia.presentation.orders.OrdersRoute
import com.example.proyectopanaderia.presentation.ticket.TicketRoute

@Composable
fun BakeryApp() {
    val owner = checkNotNull(LocalViewModelStoreOwner.current)
    val model: LoginViewModel = hiltViewModel()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val state by model.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val onEvent: (UiEvent) -> Unit = { event ->
        when (event) {
            is UiEvent.Message -> scope.launch { snackbar.showSnackbar(event.text) }
            is UiEvent.OrderCreated -> {
                navController.selectTab(AppDestinations.ORDERS)
                scope.launch { snackbar.showSnackbar("Pedido #${event.orderId} confirmado.") }
            }
        }
    }
    ObserveEvents(model.events, onEvent)
    val entry by navController.currentBackStackEntryAsState()
    if (state.loading || state.loadError) {
        ScreenSurface {
            BrandHeader()
            if (state.loading) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                Text("Preparando tu experiencia…", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            } else {
                Heading("No pudimos abrir tus preferencias")
                Text("Inténtalo de nuevo. Tus datos guardados no se han eliminado.")
                Button(onClick = model::load, modifier = Modifier.fillMaxWidth()) { Text("Reintentar") }
            }
        }
        return
    }

    // Replace the entire session stack so Back cannot reopen protected screens after logout.
    LaunchedEffect(state.signedInEmail, entry?.destination?.route) {
        val route = entry?.destination?.route ?: return@LaunchedEffect
        if (state.signedInEmail.isNotBlank() && route == AppDestinations.LOGIN) {
            navController.replaceSession(AppDestinations.SHOP)
        } else if (state.signedInEmail.isBlank() &&
            route != AppDestinations.LOGIN && route != AppDestinations.SETTINGS) {
            navController.replaceSession(AppDestinations.LOGIN)
        }
    }
    val openSettings: () -> Unit = {
        model.clearAuthError()
        navController.navigate(AppDestinations.SETTINGS) { launchSingleTop = true }
    }
    Box(Modifier.fillMaxSize()) {
    NavHost(navController, startDestination = AppDestinations.LOGIN, route = AppDestinations.ROOT) {
        composable(AppDestinations.LOGIN) {
            if (state.signedInEmail.isBlank()) LoginRoute(owner, openSettings)
        }
        composable(AppDestinations.SETTINGS) {
            SettingsRoute(owner) { navController.popBackStack() }
        }

        navigation(startDestination = AppDestinations.CATALOG, route = AppDestinations.SHOP) {
            listOf(AppDestinations.CATALOG, AppDestinations.FAVORITES, AppDestinations.CART,
                AppDestinations.ORDERS).forEach { route ->
                composable(route) { destination ->
                    if (state.signedInEmail.isNotBlank()) ShopRoute(
                        destination, navController, state,
                        openSettings, model::signOut, onEvent,
                    )
                }
            }
            composable(AppDestinations.TICKET,
                arguments = listOf(navArgument(AppDestinations.ORDER_ID) { type = NavType.LongType }),
            ) { destination ->
                if (state.signedInEmail.isNotBlank()) ShopRoute(
                    destination, navController, state,
                    openSettings, model::signOut, onEvent,
                )
            }
        }
    }
    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 80.dp))
    }
}

private fun NavHostController.replaceSession(destination: String) {
    // Nested routes can only be resolved while their graph is active (never from Login).
    if (currentDestination?.hierarchy?.any { it.route == AppDestinations.SHOP } == true) {
        listOf(AppDestinations.FAVORITES, AppDestinations.CART,
            AppDestinations.ORDERS).forEach { clearBackStack(it) }
    }
    navigate(destination) {
        popUpTo(AppDestinations.ROOT) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.selectTab(destination: String) {
    if (currentDestination?.route == destination) return
    // A ticket is a detail of Orders, not a separate bottom-bar stack.
    if (currentDestination?.route == AppDestinations.TICKET) popBackStack()
    if (currentDestination?.route == destination) return
    navigate(destination) {
        popUpTo(AppDestinations.CATALOG) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun ShopRoute(entry: NavBackStackEntry, navController: NavHostController,
    session: LoginState, settings: () -> Unit,
    signOut: () -> Unit, onEvent: (UiEvent) -> Unit) {
    val shopEntry = remember(entry) { navController.getBackStackEntry(AppDestinations.SHOP) }
    val model = hiltViewModel<ShopViewModel, ShopViewModel.Factory>(viewModelStoreOwner = shopEntry,
        creationCallback = { it.create(session.signedInEmail) })
    val state by model.state.collectAsStateWithLifecycle()
    val destination = entry.destination.route.orEmpty()
    ShopContent(state, model::load, destination, navController::selectTab, settings, signOut,
        session.busy) { modifier ->
        when (destination) {
            AppDestinations.TICKET -> TicketRoute(session.signedInEmail,
                checkNotNull(entry.arguments).getLong(AppDestinations.ORDER_ID),
                onBack = { navController.popBackStack() }, modifier = modifier)
            AppDestinations.ORDERS -> OrdersRoute(session.signedInEmail, onEvent,
                onViewTicket = { orderId ->
                    navController.navigate(AppDestinations.ticket(orderId)) { launchSingleTop = true }
                }, modifier = modifier)
            AppDestinations.FAVORITES -> FavoritesRoute(session.signedInEmail, onEvent, modifier)
            AppDestinations.CART -> CartRoute(session.signedInEmail, onEvent, modifier)
            AppDestinations.CATALOG -> CatalogRoute(session.signedInEmail, onEvent, modifier)
        }
    }
}
