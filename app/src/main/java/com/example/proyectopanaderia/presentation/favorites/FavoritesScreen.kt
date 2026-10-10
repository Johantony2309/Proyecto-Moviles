package com.example.proyectopanaderia.presentation.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.domain.model.Product
import com.example.proyectopanaderia.domain.validation.Money
import com.example.proyectopanaderia.presentation.components.EmptyState
import com.example.proyectopanaderia.presentation.components.Heading
import com.example.proyectopanaderia.presentation.components.ProductPicture

@Composable
fun FavoritesScreen(
    favorites: List<Product>,
    busy: Boolean,
    addToCart: (Long) -> Unit,
    removeFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Heading("Tus favoritos")
                Text("Tus antojos guardados de Pastelería Andony.")
            }
        }
        if (favorites.isEmpty()) {
            item {
                EmptyState(
                    "Aún no guardaste favoritos",
                    "Explora el catálogo y guarda los productos que quieras disfrutar después.",
                )
            }
        } else {
            items(favorites, key = { it.id }) { product ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ProductPicture(product.name, Modifier.size(88.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text(Money.format(product.priceCents), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            if (!product.available) Text("No disponible", style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = { addToCart(product.id) },
                                enabled = !busy && product.available,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Añadir al carrito") }
                            TextButton(
                                onClick = { removeFavorite(product.id) },
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Quitar de favoritos") }
                        }
                    }
                }
            }
        }
    }
}
