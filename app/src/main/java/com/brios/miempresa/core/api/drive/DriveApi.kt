package com.brios.miempresa.core.api.drive

import android.content.Context
import com.brios.miempresa.R
import com.brios.miempresa.core.auth.GoogleAuthClient
import com.brios.miempresa.core.di.IoDispatcher
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DriveApi
    @Inject
    constructor(
        private val googleAuthClient: GoogleAuthClient,
        @ApplicationContext private val context: Context,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) {
        private val mainFolderName = context.getString(R.string.main_folder_name)
        private val spreadsheetNameToSearch = context.getString(R.string.spreadsheet_name, "")

        suspend fun findMainFolder(): File? =
            withContext(ioDispatcher) {
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
            withContext(ioDispatcher) {
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
            withContext(ioDispatcher) {
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
            withContext(ioDispatcher) {
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
            withContext(ioDispatcher) {
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

        suspend fun createPrivateSpreadsheet(
            parentFolderId: String,
            companyName: String,
        ): Spreadsheet? =
            withContext(ioDispatcher) {
                val sheetsService = googleAuthClient.getGoogleSheetsService()
                val driveService = googleAuthClient.getGoogleDriveService()

                sheetsService?.let {
                    val title = "$companyName${context.getString(R.string.private_sheet_suffix)}"
                    val tabNames =
                        listOf(
                            context.getString(R.string.tab_info),
                            context.getString(R.string.tab_products),
                            context.getString(R.string.tab_categories),
                            context.getString(R.string.tab_pedidos),
                        )

                    val spreadsheet =
                        Spreadsheet().apply {
                            properties = SpreadsheetProperties().setTitle(title)
                            sheets =
                                tabNames.map { name ->
                                    Sheet().apply { properties = SheetProperties().setTitle(name) }
                                }
                        }

                    val created = sheetsService.spreadsheets().create(spreadsheet).execute()

                    driveService?.let { drive ->
                        drive.files().update(created.spreadsheetId, null)
                            .setAddParents(parentFolderId)
                            .setFields("id, parents")
                            .execute()
                    }

                    return@withContext created
                }

                return@withContext null
            }

        suspend fun createPublicSpreadsheet(
            parentFolderId: String,
            companyName: String,
        ): Spreadsheet? =
            withContext(ioDispatcher) {
                val sheetsService = googleAuthClient.getGoogleSheetsService()
                val driveService = googleAuthClient.getGoogleDriveService()

                sheetsService?.let {
                    val title = "$companyName${context.getString(R.string.public_sheet_suffix)}"
                    val tabNames =
                        listOf(
                            context.getString(R.string.tab_info),
                            context.getString(R.string.tab_products),
                        )

                    val spreadsheet =
                        Spreadsheet().apply {
                            properties = SpreadsheetProperties().setTitle(title)
                            sheets =
                                tabNames.map { name ->
                                    Sheet().apply { properties = SheetProperties().setTitle(name) }
                                }
                        }

                    val created = sheetsService.spreadsheets().create(spreadsheet).execute()

                    driveService?.let { drive ->
                        drive.files().update(created.spreadsheetId, null)
                            .setAddParents(parentFolderId)
                            .setFields("id, parents")
                            .execute()

                        val permission =
                            Permission().apply {
                                type = "anyone"
                                role = "reader"
                            }
                        drive.permissions().create(created.spreadsheetId, permission).execute()
                    }

                    return@withContext created
                }

                return@withContext null
            }

        suspend fun writeInfoTab(
            spreadsheetId: String,
            infoData: List<List<String>>,
        ): Boolean =
            withContext(ioDispatcher) {
                val sheetsService = googleAuthClient.getGoogleSheetsService() ?: return@withContext false

                try {
                    val tabName = context.getString(R.string.tab_info)
                    val range = "$tabName!A1:B${infoData.size}"
                    val valueRange =
                        ValueRange()
                            .setRange(range)
                            .setMajorDimension("ROWS")
                            .setValues(infoData.map { it as List<Any> })

                    sheetsService.spreadsheets().values()
                        .update(spreadsheetId, range, valueRange)
                        .setValueInputOption("RAW")
                        .execute()

                    return@withContext true
                } catch (e: Exception) {
                    return@withContext false
                }
            }

        suspend fun initializeSheetHeaders(
            spreadsheetId: String,
            tabName: String,
            headers: List<String>,
        ): Boolean =
            withContext(ioDispatcher) {
                val sheetsService = googleAuthClient.getGoogleSheetsService() ?: return@withContext false

                try {
                    val lastCol = ('A' + headers.size - 1)
                    val range = "$tabName!A1:${lastCol}1"
                    val valueRange =
                        ValueRange()
                            .setRange(range)
                            .setMajorDimension("ROWS")
                            .setValues(listOf(headers as List<Any>))

                    sheetsService.spreadsheets().values()
                        .update(spreadsheetId, range, valueRange)
                        .setValueInputOption("RAW")
                        .execute()

                    return@withContext true
                } catch (e: Exception) {
                    return@withContext false
                }
            }

        suspend fun uploadFile(
            file: java.io.File,
            mimeType: String,
            parentFolderId: String,
            fileName: String,
        ): String? =
            withContext(ioDispatcher) {
                val driveService = googleAuthClient.getGoogleDriveService() ?: return@withContext null

                val fileMetadata =
                    File().apply {
                        name = fileName
                        parents = listOf(parentFolderId)
                    }
                val mediaContent = FileContent(mimeType, file)

                val uploadedFile =
                    driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute()

                return@withContext uploadedFile.id
            }

        suspend fun deleteFile(fileId: String): Boolean =
            withContext(ioDispatcher) {
                try {
                    val driveService = googleAuthClient.getGoogleDriveService() ?: return@withContext false
                    driveService.files().delete(fileId).execute()
                    return@withContext true
                } catch (e: Exception) {
                    return@withContext false
                }
            }

        suspend fun makeFilePublic(fileId: String): Boolean =
            withContext(ioDispatcher) {
                try {
                    val driveService = googleAuthClient.getGoogleDriveService() ?: return@withContext false

                    val permission =
                        Permission().apply {
                            type = "anyone"
                            role = "reader"
                        }
                    driveService.permissions().create(fileId, permission).execute()

                    return@withContext true
                } catch (e: Exception) {
                    return@withContext false
                }
            }
    }
