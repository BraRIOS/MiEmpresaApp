package com.brios.miempresa.navigation

import android.net.Uri

/**
 * Centralized route contract used by Navigation Compose.
 * Keeps route patterns and route builders in one place.
 */
object MiEmpresaRoutes {
    const val authGraph = "AuthGraph"
    const val clientGraph = "ClientGraph"
    const val adminGraph = "AdminGraph"

    const val welcome = "Welcome"
    const val signIn = "SignIn"
    const val onboarding = "Onboarding"
    const val onboardingModeArg = "mode"
    const val onboardingWithModePattern = "Onboarding?$onboardingModeArg={$onboardingModeArg}"
    const val home = "Home"
    const val myStores = "MyStores"
    const val editCompanyData = "EditCompanyData"
    const val ordersList = "PedidosList"
    const val orderManual = "PedidoManual"

    object ClientCatalog {
        const val companyIdArg = "companyId"
        const val pattern = "ClientCatalog/{$companyIdArg}"
        fun create(companyId: String) = "ClientCatalog/$companyId"
    }

    object Cart {
        const val companyIdArg = "companyId"
        const val pattern = "Cart/{$companyIdArg}"
        fun create(companyId: String) = "Cart/$companyId"
    }

    object ProductDetail {
        const val productIdArg = "productId"
        const val companyIdArg = "companyId"
        const val modeArg = "mode"
        const val pattern = "ProductDetail/{$productIdArg}/{$companyIdArg}?$modeArg={$modeArg}"
        fun create(
            productId: String,
            companyId: String,
            mode: String = "admin",
        ) = "ProductDetail/$productId/$companyId?mode=$mode"
    }

    object DeeplinkError {
        const val errorTypeArg = "errorType"
        const val sheetIdArg = "sheetId"
        const val pattern = "DeeplinkError/{$errorTypeArg}?$sheetIdArg={$sheetIdArg}"
        fun create(
            errorType: String,
            sheetId: String?,
        ): String {
            val encodedSheetId = sheetId?.let(Uri::encode)
            return "DeeplinkError/$errorType?sheetId=$encodedSheetId"
        }
    }

    object ProductForm {
        const val productIdArg = "productId"
        const val add = "Product/add"
        const val pattern = "Product/{$productIdArg}"
        fun create(productId: String) = "Product/$productId"
    }

    object CategoryForm {
        const val categoryIdArg = "categoryId"
        const val add = "Categories/add"
        const val pattern = "Categories/{$categoryIdArg}"
        fun create(categoryId: String) = "Categories/$categoryId"
    }

    object OrderDetail {
        const val orderIdArg = "orderId"
        const val pattern = "PedidoDetail/{$orderIdArg}"
        fun create(orderId: String) = "PedidoDetail/$orderId"
    }

fun onboarding(mode: String? = null): String =
    if (mode.isNullOrBlank()) onboarding else "$onboarding?mode=$mode"
}
