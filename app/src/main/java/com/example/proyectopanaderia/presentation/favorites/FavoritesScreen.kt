package com.example.proyectopanaderia.presentation.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.proyectopanaderia.presentation.common.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.domain.model.Product
import com.example.proyectopanaderia.domain.validation.Money
import com.example.proyectopanaderia.presentation.components.EmptyState
import com.example.proyectopanaderia.presentation.components.Heading
import com.example.proyectopanaderia.presentation.components.ProductPicture
import com.example.proyectopanaderia.presentation.components.AdaptiveScreenGrid

@Composable
fun FavoritesContent(state: ScreenState<List<Product>>, retry: () -> Unit,
    addToCart: (Long) -> Unit, removeFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContent(state, retry, modifier) { favorites, busy ->
        AdaptiveScreenGrid(modifier) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Heading("Tus favoritos", textAlign = TextAlign.Center)
                    Text("Tus antojos guardados de Pastelería Andony.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
            if (favorites.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        "Aún no guardaste favoritos",
                        "Explora el catálogo y guarda los productos que quieras disfrutar después.",
                    )
                }
            } else {
                items(favorites, key = { it.id }) { product ->
                    Card(modifier = Modifier.testTag("favorite-${product.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        BoxWithConstraints {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                maxItemsInEachRow = if (maxWidth < 300.dp * LocalDensity.current.fontScale) 1 else 2,
                            ) {
                                ProductPicture(product.imageKey, Modifier.size(88.dp))
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
    }
}
