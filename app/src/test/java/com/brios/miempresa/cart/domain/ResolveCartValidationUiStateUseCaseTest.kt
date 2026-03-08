package com.brios.miempresa.cart.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveCartValidationUiStateUseCaseTest {
    private val useCase = ResolveCartValidationUiStateUseCase()

    @Test
    fun `forces blocked state while offline`() {
        val uiState =
            useCase(
                isOnline = false,
                validationResult = PriceValidationResult.AllValid,
            )

        assertTrue(uiState.blocked)
        assertTrue(uiState.effectiveValidationResult is PriceValidationResult.Blocked)
    }

    @Test
    fun `keeps items unavailable validation blocked while online`() {
        val uiState =
            useCase(
                isOnline = true,
                validationResult =
                    PriceValidationResult.ItemsUnavailable(
                        unavailableProducts = emptyList(),
                        availableTotal = 0.0,
                    ),
            )

        assertTrue(uiState.blocked)
        assertTrue(uiState.effectiveValidationResult is PriceValidationResult.ItemsUnavailable)
    }

    @Test
    fun `keeps prices updated as non blocked state while online`() {
        val uiState =
            useCase(
                isOnline = true,
                validationResult =
                    PriceValidationResult.PricesUpdated(
                        changes = emptyList(),
                        newTotal = 1500.0,
                    ),
            )

        assertFalse(uiState.blocked)
        assertTrue(uiState.effectiveValidationResult is PriceValidationResult.PricesUpdated)
    }
}
