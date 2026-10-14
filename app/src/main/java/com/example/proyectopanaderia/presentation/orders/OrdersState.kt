package com.example.proyectopanaderia.presentation.orders
import com.example.proyectopanaderia.domain.model.Order
import com.example.proyectopanaderia.domain.model.OrderStatus

data class OrdersState(val orders: List<Order>, val filter: OrderStatus?, val expandedId: Long?)
