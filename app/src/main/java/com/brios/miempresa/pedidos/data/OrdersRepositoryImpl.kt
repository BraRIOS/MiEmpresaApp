package com.brios.miempresa.pedidos.data

import android.util.Log
import com.brios.miempresa.core.api.sheets.SpreadsheetsApi
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.pedidos.domain.OrdersRepository
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
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

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val rows = dirtyOrders.map { order ->
                val items = orderDao.getItemsByOrderId(order.id)
                val itemsJson = serializeItems(items)
                val dateStr = dateFormat.format(Date(order.createdAt))
                listOf<Any>(
                    order.id,
                    dateStr,
                    order.customerName,
                    order.customerPhone ?: "",
                    order.notes ?: "",
                    itemsJson,
                    order.totalAmount,
                )
            }

            try {
                sheetsApi.appendRows(
                    spreadsheetId = privateSheetId,
                    range = "$TAB_NAME!A:G",
                    values = rows,
                )
                val now = System.currentTimeMillis()
                orderDao.markSynced(dirtyOrders.map { it.id }, now, companyId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync orders to Sheets", e)
            }
        }

        private fun serializeItems(items: List<OrderItemEntity>): String {
            val jsonArray = JSONArray()
            items.forEach { item ->
                val obj = JSONObject().apply {
                    put("name", item.productName)
                    put("qty", item.quantity)
                    put("price", item.priceAtOrder)
                }
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }

        companion object {
            private const val TAG = "OrdersRepositoryImpl"
            private const val TAB_NAME = "Pedidos"
        }
    }
