package com.brios.miempresa.orders.domain

import com.brios.miempresa.orders.data.OrderEntity
import com.brios.miempresa.orders.data.OrderItemEntity
import kotlinx.coroutines.flow.Flow

interface OrdersRepository {
    fun getAllOrders(companyId: String): Flow<List<OrderEntity>>

    suspend fun getOrderById(id: String, companyId: String): OrderEntity?

    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>>

    suspend fun createOrder(
        order: OrderEntity,
        items: List<OrderItemEntity>,
    )

    suspend fun syncPendingChanges(companyId: String)
}
