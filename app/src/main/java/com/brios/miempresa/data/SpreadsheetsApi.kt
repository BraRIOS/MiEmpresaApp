package com.brios.miempresa.data

import javax.inject.Inject


class SpreadsheetsApi @Inject constructor() {
//    suspend fun readDataFromSheet(context: Context): ValueRange? {
//        val credentials
//        return if (credentials != null) {
//            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
//            val jsonFactory = GsonFactory.getDefaultInstance()
//            val service = Sheets.Builder(httpTransport, jsonFactory, credentials)
//                .setApplicationName("MiEmpresa")
//                .build()
//
////            val driveService = Drive.Builder(transport, jsonFactory, credential)
////                .setApplicationName("YourAppName")
////                .build()
////            val files = driveService.files().list()
////                .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
////                .execute()
//
//            val spreadsheetId = "12LPzfQCaaoak8OWmvyaHK3nTIJtQnc5S"
//            val range = "'Productos'!A1:E30"
//            service.spreadsheets().values()
//                .get(spreadsheetId, range)
//                .execute()
//        } else {
//            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Sin acceso", Toast.LENGTH_SHORT).show()
//            }
//            null
//        }
//    }
}