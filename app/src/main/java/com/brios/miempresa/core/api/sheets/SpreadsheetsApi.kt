package com.brios.miempresa.core.api.sheets

import android.content.Context
import android.util.Log
import com.brios.miempresa.R
import com.brios.miempresa.core.auth.GoogleAuthClient
import com.brios.miempresa.core.di.IoDispatcher
import com.brios.miempresa.products.data.ProductEntity
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.ClearValuesRequest
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.ValueRange
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

// TODO: Restore after refactor
// import com.brios.miempresa.categories.Category
// import com.brios.miempresa.product.Product

class SpreadsheetsApi
    @Inject
    constructor(
        private val googleAuthClient: GoogleAuthClient,
        @ApplicationContext private val context: Context,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
            withContext(ioDispatcher) {
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
        }

        suspend fun getSheetId(
            spreadsheetId: String,
            sheetName: String,
        ): Int? =
            withContext(ioDispatcher) {
                val service = googleAuthClient.getGoogleSheetsService()
                val spreadsheet = service?.spreadsheets()?.get(spreadsheetId)?.execute()
                val sheet = spreadsheet?.sheets?.find { it.properties?.title == sheetName }
                sheet?.properties?.sheetId
            }

        suspend fun readRange(
            spreadsheetId: String,
            range: String,
        ): List<List<Any>>? =
            withContext(ioDispatcher) {
                val service = googleAuthClient.getGoogleSheetsService()
                if (service == null) {
                    android.util.Log.e("SpreadsheetsApi", "GoogleSheetsService is null - authentication required")
                    return@withContext null
                }
                val response = service.spreadsheets().values().get(spreadsheetId, range).execute()
                response?.getValues()
            }

        suspend fun readPublicRange(
            spreadsheetId: String,
            range: String,
            apiKey: String? = null,
        ): List<List<Any>> =
            withContext(ioDispatcher) {
                val publicService =
                    Sheets
                        .Builder(
                            NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            null,
                        ).setApplicationName(context.getString(R.string.app_name))
                        .build()

                val request = publicService.spreadsheets().values().get(spreadsheetId, range)
                if (!apiKey.isNullOrBlank()) {
                    request.setKey(apiKey)
                }

                try {
                    request.execute()?.getValues() ?: emptyList()
                } catch (e: GoogleJsonResponseException) {
                    if (apiKey.isNullOrBlank() && e.statusCode in setOf(400, 401, 403)) {
                        readPublicRangeFromCsv(
                            spreadsheetId = spreadsheetId,
                            range = range,
                        )
                    } else {
                        throw e
                    }
                }
            }

        private fun readPublicRangeFromCsv(
            spreadsheetId: String,
            range: String,
        ): List<List<Any>> {
            val rangeSpec = parseRangeSpec(range)
            val encodedSheetName = URLEncoder.encode(rangeSpec.sheetName, Charsets.UTF_8.name())
            val url = URL("https://docs.google.com/spreadsheets/d/$spreadsheetId/gviz/tq?tqx=out:csv&sheet=$encodedSheetName")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            val responseCode = connection.responseCode
            if (responseCode >= 400) {
                connection.disconnect()
                throw PublicSheetHttpException(
                    statusCode = responseCode,
                    message = "Public CSV endpoint failed with HTTP $responseCode",
                )
            }

            val body =
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
            connection.disconnect()

            if (body.isBlank()) return emptyList()

            val csvRows = parseCsvRows(body)
            val rowsInRange =
                csvRows
                    .drop(rangeSpec.startRowIndex)
                    .map { row ->
                        (rangeSpec.startColumnIndex..rangeSpec.endColumnIndex).map { columnIndex ->
                            row.getOrNull(columnIndex).orEmpty()
                        }
                    }.filter { row -> row.any { it.isNotBlank() } }

            return rowsInRange.map { row -> row.map { it as Any } }
        }

        private fun parseRangeSpec(range: String): CsvRangeSpec {
            val (sheetNameRaw, cellRangeRaw) =
                range.split("!", limit = 2).let { parts ->
                    val sheet = parts.getOrNull(0)?.trim().orEmpty()
                    val cells = parts.getOrNull(1)?.trim().orEmpty()
                    sheet to cells
                }

            val sheetName = sheetNameRaw.takeIf { it.isNotEmpty() } ?: "Info"
            val cellRange = cellRangeRaw.takeIf { it.isNotEmpty() } ?: "A:Z"

            val startToken = cellRange.substringBefore(":").trim().uppercase()
            val endToken = cellRange.substringAfter(":", startToken).trim().uppercase()

            val startColumnLabel = COLUMN_LABEL_REGEX.find(startToken)?.value ?: "A"
            val endColumnLabel = COLUMN_LABEL_REGEX.find(endToken)?.value ?: startColumnLabel
            val startRow = ROW_INDEX_REGEX.find(startToken)?.value?.toIntOrNull() ?: 1

            return CsvRangeSpec(
                sheetName = sheetName,
                startColumnIndex = columnLabelToIndex(startColumnLabel),
                endColumnIndex = columnLabelToIndex(endColumnLabel),
                startRowIndex = (startRow - 1).coerceAtLeast(0),
            )
        }

        private fun columnLabelToIndex(label: String): Int {
            var result = 0
            label.uppercase().forEach { char ->
                result = (result * 26) + (char.code - 'A'.code + 1)
            }
            return result - 1
        }

        private fun parseCsvRows(csv: String): List<List<String>> {
            val rows = mutableListOf<List<String>>()
            var currentRow = mutableListOf<String>()
            val currentField = StringBuilder()
            var inQuotes = false
            var index = 0

            while (index < csv.length) {
                val char = csv[index]

                when {
                    char == '"' -> {
                        if (inQuotes && index + 1 < csv.length && csv[index + 1] == '"') {
                            currentField.append('"')
                            index++
                        } else {
                            inQuotes = !inQuotes
                        }
                    }

                    char == ',' && !inQuotes -> {
                        currentRow.add(currentField.toString())
                        currentField.clear()
                    }

                    (char == '\n' || char == '\r') && !inQuotes -> {
                        if (char == '\r' && index + 1 < csv.length && csv[index + 1] == '\n') {
                            index++
                        }
                        currentRow.add(currentField.toString())
                        currentField.clear()
                        rows.add(currentRow)
                        currentRow = mutableListOf()
                    }

                    else -> currentField.append(char)
                }
                index++
            }

            currentRow.add(currentField.toString())
            if (currentRow.size > 1 || currentRow.firstOrNull()?.isNotEmpty() == true) {
                rows.add(currentRow)
            }
            return rows
        }

        suspend fun appendRows(
            spreadsheetId: String,
            range: String,
            values: List<List<Any>>,
            valueInputOption: String = "USER_ENTERED",
        ) {
            withContext(ioDispatcher) {
                val service = googleAuthClient.getGoogleSheetsService()
                val body = ValueRange().setValues(values)
                service?.spreadsheets()?.values()?.append(spreadsheetId, range, body)
                    ?.setValueInputOption(valueInputOption)
                    ?.execute()
            }
        }

        suspend fun clearAndWriteAll(
            spreadsheetId: String,
            tabName: String,
            headers: List<String>,
            rows: List<List<Any>>,
        ) {
            val service = googleAuthClient.getGoogleSheetsService() ?: return
            val lastCol = ('A' + headers.size - 1)
            val fullRange = "$tabName!A1:${lastCol}$MAX_SHEET_ROWS"
            withContext(ioDispatcher) {
                service.spreadsheets().values()
                    .clear(spreadsheetId, fullRange, ClearValuesRequest())
                    .execute()
                val allRows = mutableListOf<List<Any>>()
                allRows.add(headers)
                allRows.addAll(rows)
                val body = ValueRange().setValues(allRows)
                service.spreadsheets().values()
                    .update(spreadsheetId, "$tabName!A1", body)
                    .setValueInputOption("USER_ENTERED")
                    .execute()
            }
        }

        suspend fun hideColumns(
            spreadsheetId: String,
            tabName: String,
            columnIndices: List<Int>,
        ) {
            val service = googleAuthClient.getGoogleSheetsService() ?: return
            val sheetId = getSheetId(spreadsheetId, tabName) ?: return
            val requests =
                columnIndices.map { colIndex ->
                    Request().setUpdateDimensionProperties(
                        com.google.api.services.sheets.v4.model.UpdateDimensionPropertiesRequest()
                            .setRange(
                                DimensionRange()
                                    .setSheetId(sheetId)
                                    .setDimension("COLUMNS")
                                    .setStartIndex(colIndex)
                                    .setEndIndex(colIndex + 1),
                            )
                            .setProperties(
                                com.google.api.services.sheets.v4.model.DimensionProperties()
                                    .setHiddenByUser(true),
                            )
                            .setFields("hiddenByUser"),
                    )
                }
            withContext(ioDispatcher) {
                service.spreadsheets()
                    .batchUpdate(
                        spreadsheetId,
                        BatchUpdateSpreadsheetRequest().setRequests(requests),
                    )
                    .execute()
            }
        }

        companion object {
            private const val MAX_SHEET_ROWS = 10000
            private val COLUMN_LABEL_REGEX = Regex("[A-Z]+")
            private val ROW_INDEX_REGEX = Regex("\\d+")
            private val PUBLIC_PRICE_CLEAN_REGEX = Regex("[^0-9,.-]")
        }

        suspend fun getProductsByIds(
            spreadsheetId: String,
            productIds: List<String>,
            companyId: String,
        ): List<ProductEntity> {
            return withContext(ioDispatcher) {
                try {
                    val requestedIds = productIds.toSet()
                    val rows = readPublicRange(spreadsheetId = spreadsheetId, range = "Products!A2:E")
                    val now = System.currentTimeMillis()

                    rows.mapIndexedNotNull { index, row ->
                        val name = row.getOrNull(0)?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapIndexedNotNull null
                        val description = row.getOrNull(1)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                        val price = parsePublicPrice(row.getOrNull(2)?.toString())
                        val categoryName = row.getOrNull(3)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                        val imageUrl = normalizePublicImageUrl(row.getOrNull(4)?.toString()?.trim())
                        val resolvedId = buildPublicProductId(companyId, index, name, categoryName)

                        if (resolvedId !in requestedIds) return@mapIndexedNotNull null

                        ProductEntity(
                            id = resolvedId,
                            name = name,
                            price = price,
                            companyId = companyId,
                            description = description,
                            categoryName = categoryName,
                            imageUrl = imageUrl,
                            isPublic = true,
                            dirty = false,
                            deleted = false,
                            lastSyncedAt = now,
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("SpreadsheetsApi", "Failed to fetch products by IDs", e)
                    emptyList()
                }
            }
        }

        private fun buildPublicProductId(
            companyId: String,
            rowIndex: Int,
            name: String,
            categoryName: String?,
        ): String {
            val raw = "$companyId|$rowIndex|${name.lowercase()}|${categoryName.orEmpty().lowercase()}"
            return UUID.nameUUIDFromBytes(raw.toByteArray()).toString()
        }

        private fun parsePublicPrice(rawPrice: String?): Double {
            if (rawPrice.isNullOrBlank()) return 0.0
            val normalized = rawPrice.replace(PUBLIC_PRICE_CLEAN_REGEX, "").replace(',', '.')
            return normalized.toDoubleOrNull() ?: 0.0
        }

        private fun normalizePublicImageUrl(value: String?): String? {
            if (value.isNullOrBlank()) return null
            if (value.startsWith("http://") || value.startsWith("https://")) return value
            return "https://lh3.googleusercontent.com/d/$value"
        }

        private data class CsvRangeSpec(
            val sheetName: String,
            val startColumnIndex: Int,
            val endColumnIndex: Int,
            val startRowIndex: Int,
        )
    }

class PublicSheetHttpException(
    val statusCode: Int,
    message: String,
) : Exception(message)
