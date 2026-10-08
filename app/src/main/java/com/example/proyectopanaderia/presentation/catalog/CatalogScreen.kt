package com.example.proyectopanaderia.presentation.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.domain.model.*
import com.example.proyectopanaderia.domain.validation.Money
import com.example.proyectopanaderia.presentation.components.*

@Composable
fun CatalogScreen(
    products: List<Product>, categories: List<Category>, favoriteIds: Set<Long>,
    busy: Boolean, add: (Long) -> Unit, favorite: (Long, Boolean) -> Unit, examples: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    val filtered = products.filter {
        (categoryId == null || categories.none { c -> c.id == categoryId } || it.categoryId == categoryId) &&
            (it.name.contains(query.trim(), true) || it.description.contains(query.trim(), true))
    }
    LazyVerticalGrid(columns = GridCells.Adaptive(150.dp), modifier = modifier,
        contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Heading("Tu próximo antojo.")
                Text("Recién hecho, elegido por ti.")
                OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Buscar productos") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = categoryId == null, onClick = { categoryId = null }, label = { Text("Todo") }) }
                    items(categories, key = { it.id }) { c ->
                        FilterChip(selected = categoryId == c.id, onClick = { categoryId = c.id }, label = { Text(c.name) })
                    }
                }
            }
        }
        if (filtered.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EmptyState(if (products.isEmpty()) "Tu catálogo empieza aquí" else "No hay productos para mostrar",
                    if (products.isEmpty()) "Carga los productos de demostración para explorar el catálogo." else "Prueba otra búsqueda o selecciona Todo.")
                if (products.isEmpty()) {
                    Button(onClick = examples, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Cargar catálogo de ejemplo") }
                }
            }
        }
        items(filtered, key = { it.id }) { p ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ProductPicture(p.name, Modifier.fillMaxWidth().height(90.dp))
                    Text(p.name, fontWeight = FontWeight.Bold)
                    Text(p.description, style = MaterialTheme.typography.bodySmall)
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
