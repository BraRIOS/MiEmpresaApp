package com.brios.miempresa.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SignIn")
data object SignInRoute

@Serializable
@SerialName("Welcome")
data object WelcomeRoute

@Serializable
@SerialName("MyStores")
data object MyStoresRoute

@Serializable
@SerialName("Home")
data object HomeRoute

@Serializable
@SerialName("EditCompanyData")
data object EditCompanyDataRoute

@Serializable
@SerialName("PedidosList")
data object OrdersListRoute

@Serializable
@SerialName("PedidoManual")
data object OrderManualRoute

@Serializable
@SerialName("Product/add")
data object ProductFormAddRoute

@Serializable
@SerialName("Product")
data class ProductFormRoute(
    val productId: String,
)

@Serializable
@SerialName("Categories/add")
data object CategoryFormAddRoute

@Serializable
@SerialName("Categories")
data class CategoryFormRoute(
    val categoryId: String,
)

@Serializable
@SerialName("PedidoDetail")
data class OrderDetailRoute(
    val orderId: String,
)

@Serializable
@SerialName("ClientCatalog")
data class ClientCatalogRoute(
    val companyId: String,
)

@Serializable
@SerialName("Cart")
data class CartRoute(
    val companyId: String,
)

@Serializable
@SerialName("ProductDetail")
data class ProductDetailRoute(
    val productId: String,
    val companyId: String,
    val mode: String = "admin",
)

@Serializable
@SerialName("DeeplinkError")
data class DeeplinkErrorRoute(
    val errorType: String,
    val sheetId: String? = null,
)

@Serializable
@SerialName("Onboarding")
data class OnboardingRoute(
    val mode: String? = null,
)
