package com.brios.miempresa.orders.data

import android.util.Log
import com.brios.miempresa.core.api.sheets.SpreadsheetsApi
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.orders.domain.OrdersRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrdersRepositoryImpl
    @Inject
    constructor(
        private val orderDao: OrderDao,
        private val companyDao: CompanyDao,
        private val sheetsApi: SpreadsheetsApi,
    ) : OrdersRepository {
        override fun getAllOrders(companyId: String): Flow<List<OrderEntity>> =
            orderDao.getAllByCompanyFlow(companyId)

        override suspend fun getOrderById(id: String, companyId: String): OrderEntity? =
            orderDao.getById(id, companyId)

        override fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>> =
            orderDao.getItemsByOrderIdFlow(orderId)

        override suspend fun createOrder(order: OrderEntity, items: List<OrderItemEntity>) {
            orderDao.insertOrderWithItems(
                order.copy(dirty = true),
                items,
            )
        }

        override suspend fun syncPendingChanges(companyId: String) {
            val company = companyDao.getCompanyById(companyId) ?: return
            val privateSheetId = company.privateSheetId ?: return
            val dirtyOrders = orderDao.getDirty(companyId)
            if (dirtyOrders.isEmpty()) return

            // Duplication guard: read existing IDs from Sheet to skip already-synced rows
            val existingIds = try {
                val sheetData = sheetsApi.readRange(privateSheetId, "$TAB_NAME!A:A")
                sheetData?.mapNotNull { row -> row.getOrNull(0)?.toString() }?.toSet() ?: emptySet()
            } catch (e: Exception) {
                Log.w(TAG, "Could not read existing order IDs from Sheet, proceeding without guard", e)
                emptySet()
            }

            val ordersToSync = dirtyOrders.filter { it.id !in existingIds }
            if (ordersToSync.isEmpty()) {
                // All dirty orders already exist in Sheet, just mark them synced
                val now = System.currentTimeMillis()
                orderDao.markSynced(dirtyOrders.map { it.id }, now, companyId)
                return
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val rows = ordersToSync.map { order ->
                val items = orderDao.getItemsByOrderId(order.id)
                val itemsSummary = formatItemsSummary(items)
                val dateStr = dateFormat.format(Date(order.orderDate))
                listOf<Any>(
                    order.id,
                    order.displayOrderNumber,
                    dateStr,
                    order.customerName,
                    order.customerPhone ?: "",
                    order.notes ?: "",
                    order.totalAmount,
                    itemsSummary,
                )
            }

            try {
                sheetsApi.appendRows(
                    spreadsheetId = privateSheetId,
                    range = "$TAB_NAME!A:H",
                    values = rows,
                )
                val now = System.currentTimeMillis()
                orderDao.markSynced(dirtyOrders.map { it.id }, now, companyId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync orders to Sheets", e)
            }
        }

        /** Human-readable summary: "2x Malbec, 1x Aceite" */
        private fun formatItemsSummary(items: List<OrderItemEntity>): String =
            items.joinToString(", ") { "${it.quantity}x ${it.productName}" }

        companion object {
            private const val TAG = "OrdersRepositoryImpl"
            private const val TAB_NAME = "Pedidos"
        }
    }
