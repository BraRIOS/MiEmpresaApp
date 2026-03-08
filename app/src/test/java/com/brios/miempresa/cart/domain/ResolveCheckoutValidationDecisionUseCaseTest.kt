package com.brios.miempresa.cart.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveCheckoutValidationDecisionUseCaseTest {
    private val useCase = ResolveCheckoutValidationDecisionUseCase()

    @Test
    fun `returns proceed decision when all prices are valid`() {
        val decision = useCase(PriceValidationResult.AllValid)

        assertEquals(CheckoutValidationDecision.ProceedToWhatsApp, decision)
    }

    @Test
    fun `returns prices updated notice decision`() {
        val decision =
            useCase(
                PriceValidationResult.PricesUpdated(
                    changes = emptyList(),
                    newTotal = 2500.0,
                ),
            )

        assertEquals(CheckoutValidationDecision.ShowPricesUpdatedNotice, decision)
    }

    @Test
    fun `returns items unavailable error decision`() {
        val decision =
            useCase(
                PriceValidationResult.ItemsUnavailable(
                    unavailableProducts = emptyList(),
                    availableTotal = 0.0,
                ),
            )

        assertEquals(CheckoutValidationDecision.ShowItemsUnavailableError, decision)
    }

    @Test
    fun `returns blocked error decision`() {
        val decision = useCase(PriceValidationResult.Blocked)

        assertEquals(CheckoutValidationDecision.ShowBlockedError, decision)
    }
}
