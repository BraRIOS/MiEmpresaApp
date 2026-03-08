package com.brios.miempresa.cart.domain

sealed class PriceValidationResult {
    /**
     * ✅ Case 1: No changes detected after cart validation sync
     * UX: Enable "Send Order" button, no banners
     */
    data object AllValid : PriceValidationResult()

    /**
     * 🟡 Case 2a: Prices updated after cart validation sync
     * UX: Yellow banner "Prices updated", show old→new, enable button
     */
    data class PricesUpdated(
        val changes: List<PriceChange>,
        val newTotal: Double,
    ) : PriceValidationResult()

    /**
     * 🔴 Case 2b: Some products unavailable (deleted/hidden)
     * UX: Red banner "Products unavailable", mark items in cart, user deletes manually
     */
    data class ItemsUnavailable(
        val unavailableProducts: List<UnavailableProduct>,
        // Total excluding unavailable items
        val availableTotal: Double,
    ) : PriceValidationResult()

    /**
     * 🔴 Case 3: Validation blocked while offline
     * UX: Disable "Send Order" button, red banner "Connection required"
     */
    data object Blocked : PriceValidationResult()
}

data class PriceChange(
    val productId: String,
    val productName: String,
    val oldPrice: Double,
    val newPrice: Double,
    // newPrice - oldPrice (can be negative)
    val difference: Double,
)

data class UnavailableProduct(
    val productId: String,
    val productName: String,
    val lastKnownPrice: Double,
)
