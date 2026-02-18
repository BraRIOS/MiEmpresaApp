package com.brios.miempresa.navigation

/**
 * Centralized route contract used by Navigation Compose.
 * Keeps route patterns and route builders in one place.
 */
object MiEmpresaRoutes {
    const val authGraph = "AuthGraph"
    const val clientGraph = "ClientGraph"
    const val adminGraph = "AdminGraph"

    const val welcome = "Welcome"
    const val onboarding = "Onboarding"
    const val home = "Home"
    const val myStores = "MyStores"

    object Cart {
        fun create(companyId: String) = "Cart/$companyId"
    }
}
