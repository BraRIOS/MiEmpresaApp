package com.brios.miempresa.pedidos.domain

import com.brios.miempresa.pedidos.data.OrderEntity
import com.brios.miempresa.pedidos.data.OrderItemEntity
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
