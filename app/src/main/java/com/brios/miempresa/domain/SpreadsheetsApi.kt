package com.brios.miempresa.domain

import android.content.Context
import com.brios.miempresa.R
import com.brios.miempresa.categories.Category
import com.brios.miempresa.product.Product
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.ValueRange
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SpreadsheetsApi
    @Inject
    constructor(
        private val googleAuthClient: GoogleAuthClient,
        @ApplicationContext private val context: Context,
    ) {
        suspend fun readProductsFromSheet(spreadsheetId: String): List<Product> {
            val service = googleAuthClient.getGoogleSheetsService()
            val range = "'${context.getString(R.string.sheet_1_name)}'!A2:E"
            val data =
                service?.spreadsheets()?.values()
                    ?.get(spreadsheetId, range)
                    ?.execute()
            return data?.getValues()?.mapIndexed { index, it ->
                Product(
                    rowIndex = index + 1,
                    name = it[0].toString(),
                    description = it[1].toString(),
                    price = it[2].toString(),
                    categories = it[3].toString().split(", "),
                    imageUrl = if (it.size > 4) it[4].toString() else "",
                )
            } ?: emptyList()
        }

        suspend fun readProductFromSheet(
            spreadsheetId: String,
            rowIndex: Int,
        ): Product? {
            val service = googleAuthClient.getGoogleSheetsService()
            val range = "'${context.getString(R.string.sheet_1_name)}'!A${rowIndex + 1}:E${rowIndex + 1}"
            val valueRange = service?.spreadsheets()?.values()?.get(spreadsheetId, range)?.execute()
            val values = valueRange?.getValues()
            if (!values.isNullOrEmpty() && values[0] != null) {
                val name = values[0][0].toString()
                val description = values[0][1].toString()
                val price = values[0][2].toString()
                val categories = values[0][3].toString().split(", ")
                val imageUrl = if (values[0].size > 4) values[0][4].toString() else ""
                return Product(rowIndex, name, description, price, categories, imageUrl)
            }
            return null
        }

        suspend fun addProductInSheet(
            spreadsheetId: String,
            product: Product,
            categories: List<Category>,
        ) {
            val service = googleAuthClient.getGoogleSheetsService()
            val range = "'${context.getString(
                R.string.sheet_1_name,
            )}'!A${product.rowIndex + 1}:E${product.rowIndex + 1}"

            val categoriesFormula = buildCategoriesFormula(categories)

            val values =
                listOf(
                    listOf(
                        product.name,
                        product.description,
                        "$" + product.price,
                        categoriesFormula,
                        product.imageUrl,
                    ),
                )
            val body = ValueRange().setValues(values)
            service?.spreadsheets()?.values()?.append(spreadsheetId, range, body)
                ?.setValueInputOption("USER_ENTERED")
                ?.execute()
        }

        suspend fun updateProductInSheet(
            spreadsheetId: String,
            product: Product,
            categories: List<Category>,
        ) {
            val service = googleAuthClient.getGoogleSheetsService()
            val range = "'${context.getString(
                R.string.sheet_1_name,
            )}'!A${product.rowIndex + 1}:E${product.rowIndex + 1}"

            val categoriesFormula = buildCategoriesFormula(categories)

            val values =
                listOf(
                    listOf(
                        product.name,
                        product.description,
                        "$" + product.price,
                        categoriesFormula,
                        product.imageUrl,
                    ),
                )
            val body = ValueRange().setValues(values)
            service?.spreadsheets()?.values()?.update(spreadsheetId, range, body)
                ?.setValueInputOption("USER_ENTERED")
                ?.execute()
        }

        private fun buildCategoriesFormula(categories: List<Category>): String {
            if (categories.isEmpty()) return ""

            val categoryFormulas =
                categories.joinToString("; ") {
                    "SI.ERROR(${context.getString(R.string.sheet_2_name)}!A${it.rowIndex + 1}; \"\")"
                }

            return "=TEXTJOIN(\", \"; VERDADERO; $categoryFormulas)"
        }

        suspend fun readCategoriesFromSheet(spreadsheetId: String): List<Category> {
            val service = googleAuthClient.getGoogleSheetsService()
            val range = "'${context.getString(R.string.sheet_2_name)}'!A2:C"
            val data =
                service?.spreadsheets()?.values()
                    ?.get(spreadsheetId, range)
                    ?.execute()
            return data?.getValues()?.mapIndexed { index, it ->
                Category(
                    rowIndex = index + 1,
                    name = it[0] as String,
                    productQty = it[1].toString().toInt(),
                    imageUrl = if (it.size > 2) it[2] as String else "",
                )
            } ?: emptyList()
        }

        suspend fun addCategoryInSheet(
            spreadsheetId: String,
            newCategory: Category,
        ) {
            val service = googleAuthClient.getGoogleSheetsService()
            val range = "'${context.getString(
                R.string.sheet_2_name,
            )}'!A${newCategory.rowIndex + 1}:C${newCategory.rowIndex + 1}"
            val values =
                listOf(
                    listOf(
                        newCategory.name,
                        "=CONTARA(FILTER(${context.getString(R.string.sheet_1_name)}!A:A; ESNUMERO(HALLAR(A${newCategory.rowIndex + 1}; " +
                            "${context.getString(R.string.sheet_1_name)}!D:D))))",
                        newCategory.imageUrl,
                    ),
                )
            val body = ValueRange().setValues(values)
            service?.spreadsheets()?.values()?.append(spreadsheetId, range, body)
                ?.setValueInputOption("USER_ENTERED")
                ?.execute()
        }

        suspend fun updateCategoryInSheet(
            spreadsheetId: String,
            newCategory: Category,
        ) {
            val service = googleAuthClient.getGoogleSheetsService()
            val range = "'${context.getString(
                R.string.sheet_2_name,
            )}'!A${newCategory.rowIndex + 1}:C${newCategory.rowIndex + 1}"
            val values =
                listOf(
                    listOf(
                        newCategory.name,
                        "=CONTARA(FILTER(${context.getString(R.string.sheet_1_name)}!A:A; ESNUMERO(HALLAR(A${newCategory.rowIndex + 1}; " +
                            "${context.getString(R.string.sheet_1_name)}!D:D))))",
                        newCategory.imageUrl,
                    ),
                )
            val body = ValueRange().setValues(values)
            service?.spreadsheets()?.values()?.update(spreadsheetId, range, body)
                ?.setValueInputOption("USER_ENTERED")
                ?.execute()
        }

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
    }
