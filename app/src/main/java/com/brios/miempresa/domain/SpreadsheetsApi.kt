package com.brios.miempresa.domain

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
}