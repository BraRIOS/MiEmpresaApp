package com.brios.miempresa.orders.domain

import com.brios.miempresa.orders.data.OrderEntity
import com.brios.miempresa.orders.data.OrderItemEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

class CreateManualOrderUseCaseTest {
    @Test
    fun `invoke maps request and persists order with items`() =
        runTest {
            val repository = mock<OrdersRepository>()
            val useCase = CreateManualOrderUseCase(repository)

            val orderId =
                useCase(
                    CreateManualOrderRequest(
                        companyId = "company-1",
                        customerName = "  Juan ",
                        customerPhone = "+5491122334455",
                        notes = " entregar tarde ",
                        totalAmount = 3500.0,
                        orderDate = 12345L,
                        items = listOf(
                            CreateManualOrderItem(
                                productId = "prod-1",
                                productName = "Producto 1",
                                price = 1000.0,
                                quantity = 2,
                                thumbnailUrl = "thumb-1",
                            ),
                            CreateManualOrderItem(
                                productId = null,
                                productName = "Producto libre",
                                price = 1500.0,
                                quantity = 1,
                                thumbnailUrl = null,
                            ),
                        ),
                    ),
                )

            val orderCaptor = argumentCaptor<OrderEntity>()
            val itemsCaptor = argumentCaptor<List<OrderItemEntity>>()
            verifyBlocking(repository) { createOrder(orderCaptor.capture(), itemsCaptor.capture()) }

            val createdOrder = orderCaptor.firstValue
            val createdItems = itemsCaptor.firstValue
            assertEquals(orderId, createdOrder.id)
            assertEquals("Juan", createdOrder.customerName)
            assertEquals("entregar tarde", createdOrder.notes)
            assertTrue(createdOrder.dirty)
            assertEquals(2, createdItems.size)
            assertEquals(createdOrder.id, createdItems[0].orderId)
            assertEquals(createdOrder.companyId, createdItems[0].companyId)
            assertTrue(createdItems[0].id.startsWith("${createdOrder.id}_"))
        }
}

