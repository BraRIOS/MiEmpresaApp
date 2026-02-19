package com.brios.miempresa.cart.domain

import javax.inject.Inject
import kotlin.math.min

data class CartQuantityAdditionDecision(
    val quantityToAdd: Int,
    val reachedLimit: Boolean,
) {
    val canAdd: Boolean = quantityToAdd > 0
}

class ResolveCartQuantityAdditionUseCase
    @Inject
    constructor() {
        operator fun invoke(
            currentQuantity: Int,
            requestedQuantity: Int,
        ): CartQuantityAdditionDecision {
            val boundedCurrentQuantity = currentQuantity.coerceIn(0, MAX_CART_QUANTITY_PER_PRODUCT)
            val boundedRequestedQuantity = requestedQuantity.coerceAtLeast(1)
            val remainingCapacity = MAX_CART_QUANTITY_PER_PRODUCT - boundedCurrentQuantity

            if (remainingCapacity <= 0) {
                return CartQuantityAdditionDecision(
                    quantityToAdd = 0,
                    reachedLimit = true,
                )
            }

            val quantityToAdd = min(boundedRequestedQuantity, remainingCapacity)
            return CartQuantityAdditionDecision(
                quantityToAdd = quantityToAdd,
                reachedLimit = boundedRequestedQuantity > remainingCapacity,
            )
        }

        companion object {
            const val MAX_CART_QUANTITY_PER_PRODUCT = 99
        }
    }
