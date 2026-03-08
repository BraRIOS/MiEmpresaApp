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
import kotlin.math.max

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

        override fun getOrderItems(
            orderId: String,
            companyId: String,
        ): Flow<List<OrderItemEntity>> =
            orderDao.getItemsByOrderIdFlow(orderId, companyId)

        override suspend fun createOrder(order: OrderEntity, items: List<OrderItemEntity>) {
            val normalizedItems =
                items.map { item ->
                    item.copy(
                        orderId = order.id,
                        companyId = order.companyId,
                    )
                }
            orderDao.insertOrderWithItems(
                order.copy(dirty = true),
                normalizedItems,
            )
        }

        override suspend fun downloadFromSheets(companyId: String) {
            val company = companyDao.getCompanyById(companyId) ?: return
            val privateSheetId = company.privateSheetId ?: return
            val sheetRows = sheetsApi.readRange(privateSheetId, "$TAB_NAME!A2:H") ?: return
            val now = System.currentTimeMillis()

            val localOrders = orderDao.getAllByCompany(companyId)
            val localById = localOrders.associateBy(OrderEntity::id)
            val remoteIds = mutableSetOf<String>()

            sheetRows.forEach { row ->
                val orderId = row.getOrNull(0)?.toString()?.trim().orEmpty()
                if (orderId.isBlank()) return@forEach

                remoteIds += orderId
                val existingOrder = localById[orderId]
                if (existingOrder?.dirty == true) return@forEach

                val totalAmount =
                    row.getOrNull(6)
                        ?.toString()
                        ?.replace(",", ".")
                        ?.toDoubleOrNull()
                        ?: existingOrder?.totalAmount
                        ?: 0.0

                val order =
                    OrderEntity(
                        id = orderId,
                        companyId = companyId,
                        customerName = row.getOrNull(3)?.toString()?.trim().orEmpty(),
                        customerPhone = row.getOrNull(4)?.toString()?.trim().takeUnless { it.isNullOrBlank() },
                        notes = row.getOrNull(5)?.toString()?.trim().takeUnless { it.isNullOrBlank() },
                        totalAmount = totalAmount,
                        orderDate = parseRemoteOrderDate(row.getOrNull(2)?.toString(), existingOrder?.orderDate ?: now),
                        dirty = false,
                        lastSyncedAt = now,
                    )

                val items =
                    parseRemoteOrderItemsSummary(
                        orderId = orderId,
                        companyId = companyId,
                        summary = row.getOrNull(7)?.toString(),
                        totalAmount = totalAmount,
                    )
                orderDao.replaceOrderWithItems(order, items)
            }

            if (sheetRows.isNotEmpty()) {
                localOrders
                    .asSequence()
                    .filter { local -> local.id !in remoteIds && !local.dirty }
                    .forEach { orderDao.deleteById(it.id, companyId) }
            }
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
                val items = orderDao.getItemsByOrderId(order.id, companyId)
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

private data class ParsedSummaryItem(
    val index: Int,
    val name: String,
    val quantity: Int,
)

private val ORDER_ITEM_SUMMARY_REGEX = Regex("""^\s*(\d+)\s*x\s+(.+)$""", RegexOption.IGNORE_CASE)

internal fun parseRemoteOrderItemsSummary(
    orderId: String,
    companyId: String,
    summary: String?,
    totalAmount: Double,
): List<OrderItemEntity> {
    val tokens =
        summary
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

    if (tokens.isEmpty()) return emptyList()

    val parsedItems =
        tokens.mapIndexed { index, token ->
            val match = ORDER_ITEM_SUMMARY_REGEX.matchEntire(token)
            val quantity = match?.groupValues?.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val name = match?.groupValues?.getOrNull(2)?.trim().takeUnless { it.isNullOrBlank() } ?: token
            ParsedSummaryItem(index = index, name = name, quantity = quantity)
        }

    val totalUnits = max(parsedItems.sumOf { it.quantity }, 1)
    val estimatedUnitPrice = if (totalAmount > 0.0) totalAmount / totalUnits else 0.0

    return parsedItems.map { item ->
        OrderItemEntity(
            id = "${orderId}_remote_${item.index}",
            orderId = orderId,
            companyId = companyId,
            productId = null,
            productName = item.name,
            priceAtOrder = estimatedUnitPrice,
            quantity = item.quantity,
            thumbnailUrl = null,
        )
    }
}

internal fun parseRemoteOrderDate(rawDate: String?, fallback: Long): Long {
    val normalizedDate = rawDate?.trim().takeUnless { it.isNullOrBlank() } ?: return fallback
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { isLenient = false }
    return runCatching { formatter.parse(normalizedDate)?.time ?: fallback }.getOrDefault(fallback)
}
