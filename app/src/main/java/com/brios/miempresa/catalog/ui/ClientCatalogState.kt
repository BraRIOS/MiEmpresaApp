package com.brios.miempresa.catalog.ui

import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.products.data.ProductEntity

data class ClientCatalogUiData(
    val company: Company,
    val products: List<ProductEntity>,
    val categories: List<String>,
    val categoryProductCount: Map<String, Int>,
    val selectedCategory: String?,
    val searchQuery: String,
    val cartCount: Int,
    val isOffline: Boolean,
    val isAdminHybrid: Boolean,
)

sealed interface ClientCatalogState {
    data object Loading : ClientCatalogState

    data class Success(
        val data: ClientCatalogUiData,
    ) : ClientCatalogState

    data class Empty(
        val data: ClientCatalogUiData,
        val hasActiveFilters: Boolean,
    ) : ClientCatalogState

    data class Offline(
        val data: ClientCatalogUiData,
    ) : ClientCatalogState

    data class Error(
        val message: String,
    ) : ClientCatalogState
}
