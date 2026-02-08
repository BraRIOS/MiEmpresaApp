package com.brios.miempresa.categories.ui

import com.brios.miempresa.categories.data.Category

data class CategoryWithCount(
    val category: Category,
    val productCount: Int,
)

sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState

    data class Success(
        val categories: List<CategoryWithCount>,
        val searchQuery: String = "",
    ) : CategoriesUiState

    data object Empty : CategoriesUiState

    data class Error(val message: String) : CategoriesUiState
}
