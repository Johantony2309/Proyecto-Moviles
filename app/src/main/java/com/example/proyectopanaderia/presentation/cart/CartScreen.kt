package com.example.proyectopanaderia.presentation.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.domain.model.CartItem
import com.example.proyectopanaderia.domain.validation.Money
import com.example.proyectopanaderia.presentation.components.EmptyState
import com.example.proyectopanaderia.presentation.components.Heading
import com.example.proyectopanaderia.presentation.components.ProductPicture

@Composable
fun CartScreen(
    cart: List<CartItem>,
    busy: Boolean,
    addOne: (Long) -> Unit,
    setQuantity: (Long, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtotalCents = cart.fold(0L) { total, item -> Math.addExact(total, item.totalCents) }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Heading("Tu carrito")
                Text("Revisa tus antojos antes de continuar.")
            }
        }
        if (cart.isEmpty()) {
            item {
                EmptyState(
                    "Tu carrito está vacío",
                    "Añade productos del catálogo o de tus favoritos para preparar tu pedido.",
                )
            }
        } else {
            items(cart, key = { it.product.id }) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ProductPicture(item.product.name, Modifier.size(80.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.product.name, fontWeight = FontWeight.Bold)
                            Text(Money.format(item.product.priceCents), color = MaterialTheme.colorScheme.primary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { setQuantity(item.product.id, item.quantity - 1) },
                                    enabled = !busy,
                                ) { Text("−") }
                                Text("Cantidad: ${item.quantity}", fontWeight = FontWeight.Bold)
                                TextButton(
                                    onClick = { addOne(item.product.id) },
                                    enabled = !busy,
                                ) { Text("+") }
                            }
                            Text("Subtotal: ${Money.format(item.totalCents)}", style = MaterialTheme.typography.bodySmall)
                            TextButton(
                                onClick = { setQuantity(item.product.id, 0) },
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Quitar del carrito") }
                        }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Resumen", fontWeight = FontWeight.Bold)
                        Text("Subtotal: ${Money.format(subtotalCents)}")
                        Text("Retiro en Pastelería Andony: sin costo")
                        Text("Total: ${Money.format(subtotalCents)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
