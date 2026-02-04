package com.brios.miempresa.cart.domain

data class CartItem(
    val id: Long,
    val companyId: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String?,
    val quantity: Int,
    val addedAt: Long,
    val subtotal: Double = productPrice * quantity,
)

sealed interface CartUiState {
    data object Loading : CartUiState

    data object Empty : CartUiState

    data class Success(
        val items: List<CartItem>,
        val totalItems: Int,
        val totalPrice: Double,
        val companyName: String,
    ) : CartUiState

    data class Error(val message: String) : CartUiState
}

sealed interface CartEvent {
    data object NavigateToCheckout : CartEvent

    data object CartCleared : CartEvent

    data class ShowError(val message: String) : CartEvent

    data class ShowSnackbar(val message: String) : CartEvent
}
