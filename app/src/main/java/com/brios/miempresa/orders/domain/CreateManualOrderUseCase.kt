package com.brios.miempresa.orders.domain

import com.brios.miempresa.orders.data.OrderEntity
import com.brios.miempresa.orders.data.OrderItemEntity
import java.util.UUID
import javax.inject.Inject

data class CreateManualOrderItem(
    val productId: String?,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val thumbnailUrl: String? = null,
)

data class CreateManualOrderRequest(
    val companyId: String,
    val customerName: String,
    val customerPhone: String,
    val notes: String?,
    val totalAmount: Double,
    val orderDate: Long,
    val items: List<CreateManualOrderItem>,
)

class CreateManualOrderUseCase
    @Inject
    constructor(
        private val ordersRepository: OrdersRepository,
    ) {
        suspend operator fun invoke(request: CreateManualOrderRequest): String {
            val orderId = UUID.randomUUID().toString()
            val order =
                OrderEntity(
                    id = orderId,
                    companyId = request.companyId,
                    customerName = request.customerName.trim().ifBlank { "" },
                    customerPhone = request.customerPhone,
                    notes = request.notes?.trim()?.takeIf { it.isNotEmpty() },
                    totalAmount = request.totalAmount,
                    orderDate = request.orderDate,
                    dirty = true,
                )
            val items =
                request.items.mapIndexed { index, item ->
                    OrderItemEntity(
                        id = "${orderId}_$index",
                        orderId = orderId,
                        companyId = request.companyId,
                        productId = item.productId,
                        productName = item.productName,
                        priceAtOrder = item.price,
                        quantity = item.quantity,
                        thumbnailUrl = item.thumbnailUrl,
                    )
                }

            ordersRepository.createOrder(order, items)
            return orderId
        }
    }

