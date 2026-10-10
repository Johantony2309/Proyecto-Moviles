package com.example.proyectopanaderia.presentation.ticket

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.domain.model.Order
import com.example.proyectopanaderia.domain.model.OrderItem
import com.example.proyectopanaderia.domain.validation.Money
import com.example.proyectopanaderia.presentation.components.EmptyState
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TicketScreen(order: Order?, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val validOrder = order?.takeIf { it.ticketToken.isNotBlank() && it.items.isNotEmpty() }
    val payload = validOrder?.qrPayload
    val qrMatrix = remember(payload) {
        payload?.let { runCatching { QRCodeWriter().encode(it, BarcodeFormat.QR_CODE, 0, 0) }.getOrNull() }
    }

    if (validOrder == null || qrMatrix == null) {
        TicketUnavailable(onBack, modifier)
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            TextButton(onClick = onBack) { Text("Volver") }
        }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Pastelería Andony", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall)
                Text("Ticket de pedido #${validOrder.id}", style = MaterialTheme.typography.titleMedium)
                Text(formatDate(validOrder.createdAt), style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            Surface(
                color = Color.White,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    TicketQrCode(qrMatrix)
                }
            }
        }
        item {
            Text("Presenta este código al retirar tu pedido.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Text("Detalle", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        items(validOrder.items, key = { it.id }) { item -> TicketLine(item) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Total: ${Money.format(validOrder.totalCents)}", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text("Este ticket no es un comprobante de pago.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun TicketLine(item: OrderItem) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(item.productName, fontWeight = FontWeight.Medium)
            Text("${item.quantity} x ${Money.format(item.unitPriceCents)}", style = MaterialTheme.typography.bodySmall)
        }
        Text(Money.format(item.totalCents), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TicketQrCode(matrix: BitMatrix) {
    Canvas(Modifier.size(220.dp)) {
        val moduleSize = size.minDimension / matrix.width
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y]) {
                    drawRect(
                        color = Color.Black,
                        topLeft = androidx.compose.ui.geometry.Offset(x * moduleSize, y * moduleSize),
                        size = androidx.compose.ui.geometry.Size(moduleSize, moduleSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketUnavailable(onBack: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack) { Text("Volver") }
        EmptyState(
            "No pudimos abrir este ticket",
            "El pedido no existe o no contiene información válida para generar el código QR.",
        )
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "ES")).format(Date(timestamp))
