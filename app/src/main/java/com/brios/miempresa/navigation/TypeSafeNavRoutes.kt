package com.brios.miempresa.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SignInRoute

@Serializable
data class OrderDetailRoute(
    val orderId: String,
)
