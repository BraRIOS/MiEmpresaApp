package com.brios.miempresa.domain

import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DriveApi @Inject constructor(
    private val googleAuthClient: GoogleAuthClient
) {
    companion object{
        const val MAIN_FOLDER_NAME = "Generados con MiEmpresa app"
        const val SPREADSHEET_NAME = "Base de datos"
        const val SHEET_1_NAME = "Productos"
        const val SHEET_2_NAME = "Categorías"
    }

    suspend fun findMainFolder(): File? = withContext(Dispatchers.IO) {
        val driveService = googleAuthClient.getGoogleDriveService()

        driveService?.let {
            val query = "name = '$MAIN_FOLDER_NAME' and mimeType = 'application/vnd.google-apps.folder' and 'root' in parents"
            val result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            return@withContext result.files.firstOrNull()
        }

        return@withContext null
    }

    suspend fun listFoldersInFolder(parentFolderId: String): List<File>? = withContext(Dispatchers.IO) {
        val driveService = googleAuthClient.getGoogleDriveService()

        driveService?.let {
            val query = "'$parentFolderId' in parents and mimeType = 'application/vnd.google-apps.folder'"
            val result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            return@withContext result.files
        }

        return@withContext null
    }

    suspend fun findSpreadsheetInFolder(parentFolderId: String, spreadsheetName: String = SPREADSHEET_NAME): File? = withContext(Dispatchers.IO) {
        val driveService = googleAuthClient.getGoogleDriveService()

        driveService?.let {
            val query = "'$parentFolderId' in parents and mimeType = 'application/vnd.google-apps.spreadsheet' and name = '$spreadsheetName'"
            val result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            return@withContext result.files.firstOrNull()
        }

        return@withContext null
    }

    suspend fun createMainFolder(): File? = withContext(Dispatchers.IO) {
        val driveService = googleAuthClient.getGoogleDriveService()

        driveService?.let {
            val existingFolder = findMainFolder()
            if (existingFolder != null) {
                return@withContext existingFolder
            }

            val folderMetadata = File().apply {
                name = MAIN_FOLDER_NAME
                mimeType = "application/vnd.google-apps.folder"
                parents = listOf("root")
            }

            return@withContext driveService.files().create(folderMetadata)
                .setFields("id, name")
                .execute()
        }

        return@withContext null
    }

    suspend fun createCompanyFolder(parentFolderId: String, companyName: String): File? = withContext(Dispatchers.IO) {
        val driveService = googleAuthClient.getGoogleDriveService()

        driveService?.let {
            val query = "'$parentFolderId' in parents and mimeType = 'application/vnd.google-apps.folder' and name = '$companyName'"
            val existingFolders = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            if (existingFolders.files.isNotEmpty()) {
                return@withContext existingFolders.files.firstOrNull()
            }

            val folderMetadata = File().apply {
                name = companyName
                mimeType = "application/vnd.google-apps.folder"
                parents = listOf(parentFolderId)
            }

            return@withContext driveService.files().create(folderMetadata)
                .setFields("id, name")
                .execute()
        }

        return@withContext null
    }


    suspend fun createAndInitializeSpreadsheet(parentFolderId: String): Spreadsheet? = withContext(Dispatchers.IO) {
        val sheetsService = googleAuthClient.getGoogleSheetsService()
        val driveService = googleAuthClient.getGoogleDriveService()

        sheetsService?.let {
            // Crear la metadata de la nueva spreadsheet
            val spreadsheet = Spreadsheet().apply {
                properties = SpreadsheetProperties().setTitle(SPREADSHEET_NAME)
                sheets = listOf(
                    Sheet().apply {
                        properties = SheetProperties().setTitle(SHEET_1_NAME)
                    },
                    Sheet().apply {
                        properties = SheetProperties().setTitle(SHEET_2_NAME)
                    }
                )
            }

            // Crear la nueva spreadsheet
            val createdSpreadsheet = sheetsService.spreadsheets().create(spreadsheet).execute()

            // Mover la spreadsheet a la carpeta de la empresa
            driveService?.let {
                val fileId = createdSpreadsheet.spreadsheetId
                driveService.files().update(fileId, null)
                    .setAddParents(parentFolderId)
                    .setFields("id, parents")
                    .execute()
            }

            // Inicializar los datos de las hojas "Productos" y "Categorías"
            val valueRangeProductos = ValueRange().setRange("$SHEET_1_NAME!A1:E1").setMajorDimension("ROWS")
                .setValues(
                    listOf(
                        listOf("Nombre", "Descripción", "Precio", "Categorías", "URL Imagen")
                    )
                )

            val valueRangeCategorias = ValueRange().setRange("$SHEET_2_NAME!A1:C1").setMajorDimension("ROWS")
                .setValues(
                    listOf(
                        listOf("Nombre", "Cantidad de productos", "URL Imagen")
                    )
                )

            // Escribir los valores iniciales en ambas hojas
            sheetsService.spreadsheets().values()
                .update(createdSpreadsheet.spreadsheetId, valueRangeProductos.range, valueRangeProductos)
                .setValueInputOption("RAW")
                .execute()

            sheetsService.spreadsheets().values()
                .update(createdSpreadsheet.spreadsheetId, valueRangeCategorias.range, valueRangeCategorias)
                .setValueInputOption("RAW")
                .execute()

            return@withContext createdSpreadsheet
        }

        return@withContext null
    }

}
