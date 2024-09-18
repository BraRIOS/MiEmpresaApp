package com.brios.miempresa.domain

import com.google.api.services.sheets.v4.model.ValueRange
import javax.inject.Inject


class SpreadsheetsApi @Inject constructor(
    private val googleAuthClient: GoogleAuthClient
) {
    suspend fun readDataFromSheet(): ValueRange? {
        val service = googleAuthClient.getGoogleSheetsService()
        val spreadsheetId = "12LPzfQCaaoak8OWmvyaHK3nTIJtQnc5S"
        val range = "'Productos'!A1:E30"
        return service?.spreadsheets()?.values()
            ?.get(spreadsheetId, range)
            ?.execute()
    }
}