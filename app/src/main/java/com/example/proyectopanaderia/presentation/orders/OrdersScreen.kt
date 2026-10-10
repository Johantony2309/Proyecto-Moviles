package com.example.proyectopanaderia.presentation.orders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectopanaderia.app.AppContainer
import com.example.proyectopanaderia.domain.model.Order
import com.example.proyectopanaderia.domain.model.OrderItem
import com.example.proyectopanaderia.domain.model.OrderStatus
import com.example.proyectopanaderia.domain.repository.CustomerRepository
import com.example.proyectopanaderia.domain.repository.OrderRepository
import com.example.proyectopanaderia.domain.validation.Money
import com.example.proyectopanaderia.presentation.components.EmptyState
import com.example.proyectopanaderia.presentation.components.Heading
import com.example.proyectopanaderia.presentation.components.Message
import com.example.proyectopanaderia.presentation.ticket.TicketScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** State of the orders screen. */
data class OrdersState(
    val orders: List<Order> = emptyList(),
    val filter: OrderStatus? = null,
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val busy: Boolean = false,
    val message: String? = null,
    val messageIsError: Boolean = false,
)

/** Observes the orders of one customer. Archiving is reversible; orders are never deleted here. */
@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModel(
    private val email: String,
    private val customers: CustomerRepository,
    private val orders: OrderRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(OrdersState())
    val state = mutableState.asStateFlow()
    private var customerId = 0L
    private var observer: Job? = null
    init { load() }

    fun load() {
        observer?.cancel()
        mutableState.update { it.copy(loading = true, loadFailed = false) }
        observer = viewModelScope.launch {
            try {
                customerId = customers.ensureLocalCustomer(email)
                mutableState.map { it.filter }.distinctUntilChanged()
                    .flatMapLatest { status -> orders.orders(customerId, status) }
                    .collect { list ->
                        mutableState.update { it.copy(orders = list, loading = false, loadFailed = false) }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableState.update { it.copy(loading = false, loadFailed = true) }
            }
        }
    }

    fun setFilter(status: OrderStatus?) {
        mutableState.update { it.copy(filter = status, loading = true, loadFailed = false) }
    }

    /** Archives a saved order or recovers an archived one. The Room flow refreshes the list. */
    fun setArchived(orderId: Long, archived: Boolean) {
        if (state.value.busy || state.value.loading || state.value.loadFailed) return
        mutableState.update { it.copy(busy = true, message = null, messageIsError = false) }
        viewModelScope.launch {
            try {
                orders.setArchived(customerId, orderId, archived)
                mutableState.update {
                    it.copy(messageIsError = false, message = if (archived) {
                        "Pedido archivado. Lo encontrarás en Archivados."
                    } else {
                        "Pedido recuperado. Lo encontrarás en Guardados."
                    })
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                mutableState.update { it.copy(messageIsError = true,
                    message = e.message ?: "No pudimos actualizar el pedido.") }
            } catch (_: Exception) {
                mutableState.update { it.copy(messageIsError = true,
                    message = "No pudimos actualizar el pedido. Inténtalo de nuevo.") }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }

    fun clearMessage() { mutableState.update { it.copy(message = null, messageIsError = false) } }
}

@Composable
fun OrdersScreen(email: String, container: AppContainer, modifier: Modifier = Modifier) {
    val model: OrdersViewModel = viewModel(
        key = "orders:$email",
        factory = viewModelFactory {
            initializer { OrdersViewModel(email, container.customers, container.orders) }
        },
    )
    val state by model.state.collectAsStateWithLifecycle()
    var expandedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var ticketOrder by remember { mutableStateOf<Order?>(null) }
    val selectedTicket = ticketOrder
    if (selectedTicket != null) {
        BackHandler { ticketOrder = null }
        TicketScreen(selectedTicket, onBack = { ticketOrder = null }, modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Heading("Tus pedidos")
                Text("Consulta el historial de tus pedidos en Pastelería Andony.")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.filter == null, onClick = { model.setFilter(null) },
                    enabled = !state.busy, label = { Text("Todos") })
                FilterChip(selected = state.filter == OrderStatus.SAVED, onClick = { model.setFilter(OrderStatus.SAVED) },
                    enabled = !state.busy, label = { Text("Guardados") })
                FilterChip(selected = state.filter == OrderStatus.ARCHIVED, onClick = { model.setFilter(OrderStatus.ARCHIVED) },
                    enabled = !state.busy, label = { Text("Archivados") })
            }
        }
        if (state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.message?.let { message ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Message(message, isError = state.messageIsError)
                    TextButton(onClick = model::clearMessage) { Text("Descartar") }
                }
            }
        }
        when {
            state.loading -> item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.loadFailed -> item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EmptyState("No pudimos abrir tus pedidos", "Tus pedidos guardados no se han eliminado.")
                    Button(onClick = model::load) { Text("Reintentar") }
                }
            }
            state.orders.isEmpty() -> item {
                EmptyState(
                    if (state.filter == null) "Aún no tienes pedidos" else "No hay pedidos en este filtro",
                    if (state.filter == null) "Cuando confirmes un pedido aparecerá aquí con su detalle."
                    else "Prueba con Todos para ver el historial completo.",
                )
            }
            else -> items(state.orders, key = { it.id }) { order ->
                OrderCard(
                    order = order,
                    expanded = expandedId == order.id,
                    busy = state.busy,
                    onToggle = { expandedId = if (expandedId == order.id) null else order.id },
                    onViewTicket = { ticketOrder = order },
                    onSetArchived = model::setArchived,
                )
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    expanded: Boolean,
    busy: Boolean,
    onToggle: () -> Unit,
    onViewTicket: () -> Unit,
    onSetArchived: (Long, Boolean) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Pedido #${order.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                OrderStatusBadge(order.status)
            }
            Text(formatDate(order.createdAt), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Productos: ${order.items.sumOf { it.quantity }}", style = MaterialTheme.typography.bodySmall)
            Text("Total: ${Money.format(order.totalCents)}", color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold)
            TextButton(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
                Text(if (expanded) "Ocultar detalle" else "Ver detalle")
            }
            if (expanded) {
                HorizontalDivider()
                order.items.forEach { item -> OrderItemRow(item) }
            }
            OutlinedButton(
                onClick = onViewTicket,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Ver ticket QR") }
            if (order.status == OrderStatus.SAVED) {
                Button(
                    onClick = { onSetArchived(order.id, true) },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Archivar") }
            } else {
                Button(
                    onClick = { onSetArchived(order.id, false) },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Recuperar") }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(item.productName, fontWeight = FontWeight.Medium)
            Text("${item.quantity} x ${Money.format(item.unitPriceCents)}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(Money.format(item.totalCents), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OrderStatusBadge(status: OrderStatus) {
    val (label, container, content) = when (status) {
        OrderStatus.SAVED -> Triple("Guardado", MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer)
        OrderStatus.ARCHIVED -> Triple("Archivado", MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer)
    }
    Surface(shape = RoundedCornerShape(50), color = container, contentColor = content) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "ES")).format(Date(timestamp))
