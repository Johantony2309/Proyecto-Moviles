package com.example.proyectopanaderia.presentation.orders
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.domain.model.*
import com.example.proyectopanaderia.domain.validation.Money
import com.example.proyectopanaderia.presentation.components.*
import com.example.proyectopanaderia.presentation.common.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersContent(uiState: ScreenState<OrdersState>, retry: () -> Unit,
    setFilter: (OrderStatus?) -> Unit, toggle: (Long) -> Unit,
    setArchived: (Long, Boolean) -> Unit, onViewTicket: (Long) -> Unit,
    modifier: Modifier = Modifier) {
    ScreenContent(uiState, retry, modifier) { state, busy ->
        AdaptiveScreenGrid(modifier) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Heading("Tus pedidos", textAlign = TextAlign.Center)
                    Text("Consulta el historial de tus pedidos en Pastelería Andony.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(selected = state.filter == null, onClick = { setFilter(null) },
                        enabled = !busy, label = { Text("Todos") })
                    FilterChip(selected = state.filter == OrderStatus.SAVED, onClick = { setFilter(OrderStatus.SAVED) },
                        enabled = !busy, label = { Text("Guardados") })
                    FilterChip(selected = state.filter == OrderStatus.ARCHIVED, onClick = { setFilter(OrderStatus.ARCHIVED) },
                        enabled = !busy, label = { Text("Archivados") })
                }
            }
            if (busy) item(span = { GridItemSpan(maxLineSpan) }) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            when {
                state.orders.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        if (state.filter == null) "Aún no tienes pedidos" else "No hay pedidos en este filtro",
                        if (state.filter == null) "Cuando confirmes un pedido aparecerá aquí con su detalle."
                        else "Prueba con Todos para ver el historial completo.",
                    )
                }
                else -> items(state.orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        expanded = state.expandedId == order.id,
                        busy = busy,
                        onToggle = { toggle(order.id) },
                        onViewTicket = { onViewTicket(order.id) },
                        onSetArchived = setArchived,
                    )
                }
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
    Card(modifier = Modifier.testTag("order-${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp),
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
