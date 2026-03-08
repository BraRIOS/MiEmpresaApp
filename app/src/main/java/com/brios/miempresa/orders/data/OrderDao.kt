package com.brios.miempresa.orders.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Upsert
    suspend fun upsert(order: OrderEntity)

    @Upsert
    suspend fun upsertItems(items: List<OrderItemEntity>)

    @Transaction
    suspend fun insertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        upsert(order)
        upsertItems(items)
    }

    @Query("SELECT * FROM orders WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun getAllByCompanyFlow(companyId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE companyId = :companyId ORDER BY createdAt DESC")
    suspend fun getAllByCompany(companyId: String): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE id = :id AND companyId = :companyId")
    suspend fun getById(id: String, companyId: String): OrderEntity?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId AND companyId = :companyId")
    fun getItemsByOrderIdFlow(
        orderId: String,
        companyId: String,
    ): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId AND companyId = :companyId")
    suspend fun getItemsByOrderId(
        orderId: String,
        companyId: String,
    ): List<OrderItemEntity>

    @Query("DELETE FROM order_items WHERE orderId = :orderId AND companyId = :companyId")
    suspend fun deleteItemsByOrderId(
        orderId: String,
        companyId: String,
    )

    @Transaction
    suspend fun replaceOrderWithItems(
        order: OrderEntity,
        items: List<OrderItemEntity>,
    ) {
        upsert(order)
        deleteItemsByOrderId(order.id, order.companyId)
        if (items.isNotEmpty()) {
            upsertItems(items)
        }
    }

    @Query("SELECT * FROM orders WHERE dirty = 1 AND companyId = :companyId")
    suspend fun getDirty(companyId: String): List<OrderEntity>

    @Query(
        "UPDATE orders SET dirty = 0, lastSyncedAt = :timestamp " +
            "WHERE id IN (:ids) AND companyId = :companyId",
    )
    suspend fun markSynced(ids: List<String>, timestamp: Long, companyId: String)

    @Query("DELETE FROM orders WHERE companyId = :companyId")
    suspend fun deleteAll(companyId: String)

    @Query("DELETE FROM orders WHERE id = :id AND companyId = :companyId")
    suspend fun deleteById(
        id: String,
        companyId: String,
    )
}
