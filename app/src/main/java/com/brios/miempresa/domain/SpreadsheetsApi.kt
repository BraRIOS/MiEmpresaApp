package com.brios.miempresa.domain

import com.brios.miempresa.product.Product
import com.google.api.services.sheets.v4.model.ValueRange
import javax.inject.Inject


class SpreadsheetsApi @Inject constructor(
    private val googleAuthClient: GoogleAuthClient
) {
    suspend fun readProductsFromSheet(): ValueRange? {
        val service = googleAuthClient.getGoogleSheetsService()
        val spreadsheetId = "1QGU9qH_-57mk7VxgOJlbs3USQg-8iu_BPFHMt5b8Vk0"
        val range = "'Productos'!A2:E"
        return service?.spreadsheets()?.values()
            ?.get(spreadsheetId, range)
            ?.execute()
    }

    suspend fun addOrUpdateProductInSheet(product: Product) {
        val service = googleAuthClient.getGoogleSheetsService()
        val spreadsheetId = "1QGU9qH_-57mk7VxgOJlbs3USQg-8iu_BPFHMt5b8Vk0"
        val range = "'Productos'!A${product.rowIndex + 1}:E${product.rowIndex + 1}"

        val values = listOf(
            listOf(
                product.name,
                product.description,
                product.price,
                product.category,
                product.imageUrl
            )
        )
        val body = ValueRange().setValues(values)
        service?.spreadsheets()?.values()?.update(spreadsheetId, range, body)
            ?.setValueInputOption("USER_ENTERED")
            ?.execute()
    }

    suspend fun readProductFromSheet(rowIndex: Int): Product?{
        val service = googleAuthClient.getGoogleSheetsService()
        val spreadsheetId = "1QGU9qH_-57mk7VxgOJlbs3USQg-8iu_BPFHMt5b8Vk0"
        val range = "'Productos'!A${rowIndex + 1}:E${rowIndex + 1}"
        val valueRange = service?.spreadsheets()?.values()?.get(spreadsheetId, range)?.execute()
        val values = valueRange?.getValues()
        if (!values.isNullOrEmpty() && values[0] != null) {
            val name = values[0][0].toString()
            val description = values[0][1].toString()
            val price = values[0][2].toString()
            val category = values[0][3].toString()
            val imageUrl = values[0][4].toString()
            return Product(rowIndex, name, description, price, category, imageUrl)
        }
        return null
    }
}