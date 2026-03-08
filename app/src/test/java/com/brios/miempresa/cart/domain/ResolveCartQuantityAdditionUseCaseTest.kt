package com.brios.miempresa.cart.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveCartQuantityAdditionUseCaseTest {
    private val useCase = ResolveCartQuantityAdditionUseCase()

    @Test
    fun `returns zero when product already reached max cart quantity`() {
        val decision =
            useCase(
                currentQuantity = 99,
                requestedQuantity = 1,
            )

        assertFalse(decision.canAdd)
        assertTrue(decision.reachedLimit)
        assertEquals(0, decision.quantityToAdd)
    }

    @Test
    fun `caps quantity to remaining capacity when requested exceeds max`() {
        val decision =
            useCase(
                currentQuantity = 98,
                requestedQuantity = 5,
            )

        assertTrue(decision.canAdd)
        assertTrue(decision.reachedLimit)
        assertEquals(1, decision.quantityToAdd)
    }

    @Test
    fun `adds requested quantity when under max cart limit`() {
        val decision =
            useCase(
                currentQuantity = 3,
                requestedQuantity = 2,
            )

        assertTrue(decision.canAdd)
        assertFalse(decision.reachedLimit)
        assertEquals(2, decision.quantityToAdd)
    }
}
