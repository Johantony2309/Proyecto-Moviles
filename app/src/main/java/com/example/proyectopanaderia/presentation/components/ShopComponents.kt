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
fun ProductPicture(name: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Image(painterResource(R.drawable.ic_bakery), contentDescription = null, modifier = Modifier.padding(8.dp))
    }
}
