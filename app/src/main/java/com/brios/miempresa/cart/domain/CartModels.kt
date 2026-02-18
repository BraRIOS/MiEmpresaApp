package com.brios.miempresa.cart.domain

data class CartItem(
    val id: Long,
    val companyId: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productHidePrice: Boolean = false,
    val productImageUrl: String?,
    val quantity: Int,
    val addedAt: Long,
    val subtotal: Double = if (productHidePrice) 0.0 else productPrice * quantity,
)

sealed interface CartUiState {
    data object Loading : CartUiState

    data object Validating : CartUiState

    data object Empty : CartUiState

    data class Success(
        val items: List<CartItem>,
        val totalItems: Int,
        val totalPrice: Double,
        val hasHiddenPrices: Boolean = false,
        val companyName: String,
        val validationResult: PriceValidationResult? = null,
        val blocked: Boolean = false,
    ) : CartUiState

    data class Error(val message: String) : CartUiState
}

sealed interface CartEvent {
    data class ProceedToWhatsApp(
        val phoneNumber: String,
        val message: String,
    ) : CartEvent

    data object CartCleared : CartEvent

    data class ShowError(val message: String) : CartEvent

    data class ShowSnackbar(val message: String) : CartEvent
}
