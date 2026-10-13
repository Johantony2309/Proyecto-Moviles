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
    "croissant" -> R.drawable.croissant
    "cake" -> R.drawable.cake
    "bread" -> R.drawable.bread
    "roll" -> R.drawable.roll
    else -> R.drawable.ic_bakery
}
