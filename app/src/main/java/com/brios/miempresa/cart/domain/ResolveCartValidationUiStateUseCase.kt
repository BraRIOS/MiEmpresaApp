package com.brios.miempresa.cart.domain

import javax.inject.Inject

data class CartValidationUiState(
    val effectiveValidationResult: PriceValidationResult?,
    val blocked: Boolean,
)

class ResolveCartValidationUiStateUseCase
    @Inject
    constructor() {
        operator fun invoke(
            isOnline: Boolean,
            validationResult: PriceValidationResult?,
        ): CartValidationUiState {
            val effectiveValidationResult =
                if (!isOnline) {
                    PriceValidationResult.Blocked
                } else {
                    validationResult
                }

            val blocked =
                !isOnline ||
                    effectiveValidationResult is PriceValidationResult.Blocked ||
                    effectiveValidationResult is PriceValidationResult.ItemsUnavailable

            return CartValidationUiState(
                effectiveValidationResult = effectiveValidationResult,
                blocked = blocked,
            )
        }
    }
