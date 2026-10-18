package com.example.proyectopanaderia.presentation.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.proyectopanaderia.presentation.common.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.domain.model.*
import com.example.proyectopanaderia.domain.validation.Money
import com.example.proyectopanaderia.presentation.components.*

@Composable
fun CatalogContent(
    state: ScreenState<CatalogState>, retry: () -> Unit,
    setQuery: (String) -> Unit, setCategory: (Long?) -> Unit,
    add: (Long) -> Unit, favorite: (Long, Boolean) -> Unit, examples: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContent(state, retry, modifier) { data, busy ->
        val products = data.products
        val categories = data.categories
        val favoriteIds = data.favoriteIds
        val query = data.query
        val categoryId = data.categoryId
        AdaptiveScreenGrid(modifier) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Heading("Tu próximo antojo", textAlign = TextAlign.Center)
                    Text("Recién hecho, elegido por ti.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = query, onValueChange = setQuery, label = { Text("Buscar productos") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = categoryId == null, onClick = { setCategory(null) }, label = { Text("Todo") }) }
                        items(categories, key = { it.id }) { c ->
                            FilterChip(selected = categoryId == c.id, onClick = { setCategory(c.id) }, label = { Text(c.name) })
                        }
                    }
                }
            }
            if (products.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EmptyState(if (data.catalogEmpty) "Tu catálogo empieza aquí" else "No hay productos para mostrar",
                        if (data.catalogEmpty) "Carga los productos de demostración para explorar el catálogo." else "Prueba otra búsqueda o selecciona Todo.")
                    if (data.catalogEmpty) {
                        Button(onClick = examples, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Cargar catálogo de ejemplo") }
                    }
                }
            }
            items(products, key = { it.id }) { p ->
                Card(
                    modifier = Modifier.fillMaxHeight().testTag("product-${p.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        ProductPicture(p.imageKey, Modifier.fillMaxWidth().height(90.dp))
                        Text(p.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(p.description, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        Text(Money.format(p.priceCents), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        if (!p.available) Text("No disponible", style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { favorite(p.id, p.id !in favoriteIds) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                            Text(if (p.id in favoriteIds) "♥ Guardado" else "♡ Guardar")
                        }
                        Button(onClick = { add(p.id) }, enabled = !busy && p.available, modifier = Modifier.fillMaxWidth()) { Text("Añadir") }
                    }
                }
            }
        }
    }
}
