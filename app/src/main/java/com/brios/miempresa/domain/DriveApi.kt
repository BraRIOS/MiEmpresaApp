package com.brios.miempresa.domain

import android.content.Context
import com.brios.miempresa.R
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DriveApi
    @Inject
    constructor(
        private val googleAuthClient: GoogleAuthClient,
        @ApplicationContext private val context: Context,
    ) {
        private val mainFolderName = context.getString(R.string.main_folder_name)
        private val spreadsheetName =
            context.getString(
                R.string.spreadsheet_name,
                context.getString(R.string.do_not_delete_advice),
            )
        private val spreadsheetNameToSearch = context.getString(R.string.spreadsheet_name, "")
        private val sheet1Name = context.getString(R.string.sheet_1_name)
        private val sheet2Name = context.getString(R.string.sheet_2_name)

        suspend fun findMainFolder(): File? =
            withContext(Dispatchers.IO) {
                val driveService = googleAuthClient.getGoogleDriveService()

                driveService?.let {
                    val query = "name = '$mainFolderName' and mimeType = 'application/vnd.google-apps.folder' and 'root' in parents"
                    val result =
                        driveService.files().list()
                            .setQ(query)
                            .setSpaces("drive")
                            .setFields("files(id, name)")
                            .execute()

                    return@withContext result.files.firstOrNull()
                }

                return@withContext null
            }

        suspend fun listFoldersInFolder(parentFolderId: String): List<File>? =
            withContext(Dispatchers.IO) {
                val driveService = googleAuthClient.getGoogleDriveService()

                driveService?.let {
                    val query = "'$parentFolderId' in parents and mimeType = 'application/vnd.google-apps.folder'"
                    val result =
                        driveService.files().list()
                            .setQ(query)
                            .setSpaces("drive")
                            .setFields("files(id, name)")
                            .execute()

                    return@withContext result.files
                }

                return@withContext null
            }

        suspend fun findSpreadsheetInFolder(
            parentFolderId: String,
            spreadsheetName: String = this.spreadsheetNameToSearch,
        ): File? =
            withContext(Dispatchers.IO) {
                val driveService = googleAuthClient.getGoogleDriveService()
                driveService?.let {
                    val query =
                        "'$parentFolderId' in parents " +
                            "and mimeType = 'application/vnd.google-apps.spreadsheet' " +
                            "and name contains '${spreadsheetName.trim()}'"
                    val result =
                        driveService.files().list()
                            .setQ(query)
                            .setSpaces("drive")
                            .setFields("files(id, name)")
                            .execute()

                    return@withContext result.files.firstOrNull()
                }

                return@withContext null
            }

        suspend fun createMainFolder(): File? =
            withContext(Dispatchers.IO) {
                val driveService = googleAuthClient.getGoogleDriveService()

                driveService?.let {
                    val existingFolder = findMainFolder()
                    if (existingFolder != null) {
                        return@withContext existingFolder
                    }

                    val folderMetadata =
                        File().apply {
                            name = mainFolderName
                            mimeType = "application/vnd.google-apps.folder"
                            parents = listOf("root")
                        }

                    return@withContext driveService.files().create(folderMetadata)
                        .setFields("id, name")
                        .execute()
                }

                return@withContext null
            }

        suspend fun createCompanyFolder(
            parentFolderId: String,
            companyName: String,
        ): File? =
            withContext(Dispatchers.IO) {
                val driveService = googleAuthClient.getGoogleDriveService()

                driveService?.let {
                    val query = "'$parentFolderId' in parents and mimeType = 'application/vnd.google-apps.folder' and name = '$companyName'"
                    val existingFolders =
                        driveService.files().list()
                            .setQ(query)
                            .setSpaces("drive")
                            .setFields("files(id, name)")
                            .execute()

                    if (existingFolders.files.isNotEmpty()) {
                        return@withContext existingFolders.files.firstOrNull()
                    }

                    val folderMetadata =
                        File().apply {
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

        suspend fun createAndInitializeSpreadsheet(parentFolderId: String): Spreadsheet? =
            withContext(Dispatchers.IO) {
                val sheetsService = googleAuthClient.getGoogleSheetsService()
                val driveService = googleAuthClient.getGoogleDriveService()

                sheetsService?.let {
                    // Crear la metadata de la nueva spreadsheet
                    val spreadsheet =
                        Spreadsheet().apply {
                            properties = SpreadsheetProperties().setTitle(spreadsheetName)
                            sheets =
                                listOf(
                                    Sheet().apply {
                                        properties = SheetProperties().setTitle(sheet1Name)
                                    },
                                    Sheet().apply {
                                        properties = SheetProperties().setTitle(sheet2Name)
                                    },
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
                    val valueRangeProductos =
                        ValueRange().setRange("$sheet1Name!A1:E1").setMajorDimension("ROWS")
                            .setValues(
                                listOf(
                                    listOf(
                                        context.getString(R.string.name_column),
                                        context.getString(R.string.description_column),
                                        context.getString(R.string.price_column),
                                        context.getString(R.string.categories_column),
                                        context.getString(R.string.image_url_column),
                                    ),
                                ),
                            )

                    val valueRangeCategorias =
                        ValueRange().setRange("$sheet2Name!A1:C1").setMajorDimension("ROWS")
                            .setValues(
                                listOf(
                                    listOf(
                                        context.getString(R.string.name_column),
                                        context.getString(R.string.product_amount_column),
                                        context.getString(R.string.image_url_column),
                                    ),
                                ),
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
