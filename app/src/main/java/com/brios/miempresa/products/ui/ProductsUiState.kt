package com.brios.miempresa.products.ui

import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.products.data.ProductEntity

enum class PublicFilter {
    ALL,
    PUBLIC,
    PRIVATE,
}

data class ProductFilters(
    val searchQuery: String = "",
    val categoryId: String? = null,
    val publicFilter: PublicFilter = PublicFilter.ALL,
)

sealed interface ProductsUiState {
    data object Loading : ProductsUiState

    data class Success(
        val products: List<ProductEntity>,
        val categories: List<Category>,
        val filters: ProductFilters = ProductFilters(),
    ) : ProductsUiState

    data object Empty : ProductsUiState

    data class EmptyFiltered(
        val filters: ProductFilters,
        val categories: List<Category>,
    ) : ProductsUiState

    data class Error(val message: String) : ProductsUiState
}
