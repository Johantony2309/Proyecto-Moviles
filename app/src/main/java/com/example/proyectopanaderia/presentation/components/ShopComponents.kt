package com.example.proyectopanaderia.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.R

@Composable
fun EmptyState(title: String, description: String) {
    InfoCard { Text(title, fontWeight = FontWeight.Bold); Text(description) }
}

@Composable
fun ProductPicture(imageKey: String?, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Image(painterResource(productImageRes(imageKey)), contentDescription = null, modifier = Modifier.padding(8.dp))
    }
}

private fun productImageRes(imageKey: String?): Int = when (imageKey) {
    // Panes
    "croissant" -> R.drawable.croissant
    "bread" -> R.drawable.bread
    "guagua" -> R.drawable.guagua

    // Pasteles
    "cake" -> R.drawable.cake
    "torta_andony" -> R.drawable.torta_andony
    "torta_vainilla" -> R.drawable.torta_vainilla
    "torta_chocovainilla" -> R.drawable.torta_chocovainilla
    "torta_manabi_chocolate" -> R.drawable.torta_manabi_chocolate
    "torta_naranja" -> R.drawable.torta_naranja
    "torta_pina" -> R.drawable.torta_pina
    "torta_chocoblanco" -> R.drawable.torta_chocoblanco
    "torta_choconegro" -> R.drawable.torta_choconegro
    "torta_fresa" -> R.drawable.torta_fresa
    "torta_zanahoria" -> R.drawable.torta_zanahoria
    "torta_rompope" -> R.drawable.torta_rompope

    // Dulces
    "roll" -> R.drawable.roll
    "caja_alfajores" -> R.drawable.caja_alfajores
    "bolitas_coco" -> R.drawable.bolitas_coco
    "galletitas_chavelitas" -> R.drawable.galletitas_chavelitas
    "funda_huevitos_moyo" -> R.drawable.funda_huevitos_moyo
    "funda_rombitos" -> R.drawable.funda_rombitos
    "funda_suspiros" -> R.drawable.funda_suspiros
    "funda_galletitas_almidon" -> R.drawable.funda_galletitas_almidon

    // Postres
    "copa_chocolate" -> R.drawable.copa_chocolate
    "copa_frutos_rojos" -> R.drawable.copa_frutos_rojos
    "copa_maracuya" -> R.drawable.copa_maracuya
    "copa_oreo" -> R.drawable.copa_oreo
    "cuchariable_chocolate" -> R.drawable.cuchariable_chocolate
    "cuchariable_vainilla" -> R.drawable.cuchariable_vainilla
    "porcion_chocoflan" -> R.drawable.porcion_chocoflan
    "porcion_tres_leches" -> R.drawable.porcion_tres_leches
    "porcion_mojada_chocolate" -> R.drawable.porcion_mojada_chocolate

    // Bebidas
    "colada_morada_litro" -> R.drawable.colada_morada_litro
    "colada_morada_medio" -> R.drawable.colada_morada_medio
    "rompope" -> R.drawable.rompope

    else -> R.drawable.ic_bakery
}
