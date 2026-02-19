package com.brios.miempresa.cart.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NormalizeWhatsAppPhoneUseCaseTest {
    private val useCase = NormalizeWhatsAppPhoneUseCase()

    @Test
    fun `keeps only digits from country code and phone number`() {
        val normalized = useCase("+54 9 11-1234-5678")

        assertEquals("5491112345678", normalized)
    }

    @Test
    fun `returns null when input does not contain digits`() {
        val normalized = useCase("+-() ")

        assertNull(normalized)
    }
}
