package com.brios.miempresa.orders.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class OrdersRepositoryParsingTest {
    @Test
    fun `parse remote summary maps order and company scope`() {
        val items =
            parseRemoteOrderItemsSummary(
                orderId = "order-1",
                companyId = "company-1",
                summary = "2x Vino Malbec, 1x Aceite de Oliva",
                totalAmount = 9000.0,
            )

        assertEquals(2, items.size)
        assertTrue(items.all { it.orderId == "order-1" && it.companyId == "company-1" })
        assertEquals(2, items[0].quantity)
        assertEquals("Vino Malbec", items[0].productName)
        assertEquals(1, items[1].quantity)
        assertEquals("Aceite de Oliva", items[1].productName)
    }

    @Test
    fun `parse remote summary preserves total by estimated unit price`() {
        val items =
            parseRemoteOrderItemsSummary(
                orderId = "order-2",
                companyId = "company-2",
                summary = "1x Empanadas, 2x Gaseosa",
                totalAmount = 3000.0,
            )

        val reconstructedTotal = items.sumOf { it.priceAtOrder * it.quantity }
        assertEquals(3000.0, reconstructedTotal, 0.001)
    }

    @Test
    fun `parse remote summary returns empty when summary is blank`() {
        val items =
            parseRemoteOrderItemsSummary(
                orderId = "order-3",
                companyId = "company-3",
                summary = "   ",
                totalAmount = 0.0,
            )

        assertTrue(items.isEmpty())
    }

    @Test
    fun `parse remote date falls back when invalid`() {
        val fallback = 123456789L
        val parsed = parseRemoteOrderDate("invalid-date", fallback)
        assertEquals(fallback, parsed)
    }

    @Test
    fun `parse remote date parses expected format`() {
        val value = "2026-02-16 10:30"
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { isLenient = false }
        val expected = formatter.parse(value)?.time

        val parsed = parseRemoteOrderDate(value, 0L)
        assertEquals(expected, parsed)
    }
}
