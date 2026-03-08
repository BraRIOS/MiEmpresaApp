package com.brios.miempresa.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateClearingBackStack(route: String) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
    }
}

fun <T : Any> NavHostController.navigateClearingBackStack(route: T) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
    }
}
