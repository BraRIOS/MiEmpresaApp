package com.brios.miempresa.domain

import android.content.Context
import android.util.Log
import com.brios.miempresa.data.ProductEntity
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.ValueRange
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// TODO: Restore after refactor
// import com.brios.miempresa.categories.Category
// import com.brios.miempresa.product.Product

class SpreadsheetsApi
    @Inject
    constructor(
        private val googleAuthClient: GoogleAuthClient,
        @ApplicationContext private val context: Context,
    ) {
        // TODO: Restore after refactor (online-first methods using Product/Category)
        // suspend fun readProductsFromSheet(spreadsheetId: String): List<Product> { ... }
        // suspend fun readProductFromSheet(spreadsheetId: String, rowIndex: Int): Product? { ... }
        // suspend fun addProductInSheet(spreadsheetId: String, product: Product, categories: List<Category>) { ... }
        // suspend fun updateProductInSheet(spreadsheetId: String, product: Product, categories: List<Category>) { ... }
        // private fun buildCategoriesFormula(categories: List<Category>): String { ... }
        // suspend fun readCategoriesFromSheet(spreadsheetId: String): List<Category> { ... }
        // suspend fun addCategoryInSheet(spreadsheetId: String, newCategory: Category) { ... }
        // suspend fun updateCategoryInSheet(spreadsheetId: String, newCategory: Category) { ... }

        suspend fun deleteElementFromSheet(
            spreadsheetId: String,
            rowIndex: Int,
            workingSheetId: Int,
        ) {
            val service = googleAuthClient.getGoogleSheetsService()
            val deleteDimensionRequest =
                Request().apply {
                    deleteDimension =
                        DeleteDimensionRequest().apply {
                            range =
                                DimensionRange().apply {
                                    sheetId = workingSheetId
                                    dimension = "ROWS"
                                    startIndex = rowIndex
                                    endIndex = rowIndex + 1
                                }
                        }
                }
            val batchUpdateRequest =
                BatchUpdateSpreadsheetRequest().apply {
                    requests = listOf(deleteDimensionRequest)
                }
            service?.spreadsheets()?.batchUpdate(spreadsheetId, batchUpdateRequest)?.execute()
        }

        suspend fun getSheetId(
            spreadsheetId: String,
            sheetName: String,
        ): Int? {
            val service = googleAuthClient.getGoogleSheetsService()
            val spreadsheet = service?.spreadsheets()?.get(spreadsheetId)?.execute()
            val sheet = spreadsheet?.sheets?.find { it.properties?.title == sheetName }
            return sheet?.properties?.sheetId
        }

        suspend fun readRange(
            spreadsheetId: String,
            range: String,
        ): List<List<Any>>? {
            val service = googleAuthClient.getGoogleSheetsService()
            if (service == null) {
                android.util.Log.e("SpreadsheetsApi", "GoogleSheetsService is null - authentication required")
                return null
            }
            val response = service.spreadsheets().values().get(spreadsheetId, range).execute()
            return response?.getValues()
        }

        suspend fun appendRows(
            spreadsheetId: String,
            range: String,
            values: List<List<Any>>,
            valueInputOption: String = "USER_ENTERED",
        ) {
            val service = googleAuthClient.getGoogleSheetsService()
            val body = ValueRange().setValues(values)
            service?.spreadsheets()?.values()?.append(spreadsheetId, range, body)
                ?.setValueInputOption(valueInputOption)
                ?.execute()
        }

        /**
         * SPIKE S4 SIMPLIFIED: Fetches ALL products, filters in-memory.
         * OPTIMIZATION DEFERRED: Use Sheet FILTER() formula or batch queries in User Stories.
         *
         * For spike validation with ~10 products, this is acceptable.
         * Production optimization: See docs/plans/2026-02-04-spike-s4-price-validation.md §"Known Optimizations"
         */
        suspend fun getProductsByIds(
            spreadsheetId: String,
            productIds: List<String>,
            companyId: String,
        ): List<ProductEntity> {
            return withContext(Dispatchers.IO) {
                try {
                    val service = googleAuthClient.getGoogleSheetsService()

                    // Fetch ALL products from Sheet Public "Products" tab
                    val response =
                        service?.spreadsheets()?.values()
                            ?.get(spreadsheetId, "Products!A2:E") // A=id, B=name, C=price, D=isAvailable, E=unused
                            ?.execute()

                    val rows = response?.getValues() ?: emptyList()

                    // Parse and filter by productIds
                    rows.mapNotNull { row ->
                        if (row.size < 3) return@mapNotNull null
                        val id = row[0]?.toString() ?: return@mapNotNull null

                        // Filter: Only return products in cart
                        if (id !in productIds) return@mapNotNull null

                        val name = row[1]?.toString() ?: "Unknown"
                        val price = row[2]?.toString()?.toDoubleOrNull() ?: 0.0
                        val isAvailable = row.getOrNull(3)?.toString()?.toBoolean() ?: true

                        ProductEntity(
                            id = id,
                            name = name,
                            price = price,
                            companyId = companyId,
                            isAvailable = isAvailable,
                            lastSyncedAt = System.currentTimeMillis(),
                        )
                    }
                } catch (e: Exception) {
                    Log.e("SpreadsheetsApi", "Failed to fetch products by IDs", e)
                    emptyList()
                }
            }
        }
    }
