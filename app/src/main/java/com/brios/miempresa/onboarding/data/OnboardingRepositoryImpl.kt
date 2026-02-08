package com.brios.miempresa.onboarding.data

import com.brios.miempresa.core.api.drive.DriveApi
import com.brios.miempresa.core.api.sheets.SpreadsheetsApi
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.onboarding.domain.OnboardingRepository
import com.brios.miempresa.onboarding.domain.WorkspaceCreationResult
import com.brios.miempresa.onboarding.domain.WorkspaceSetupRequest
import com.brios.miempresa.onboarding.domain.WorkspaceStep
import com.brios.miempresa.onboarding.domain.WorkspaceValidationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID
import javax.inject.Inject

class OnboardingRepositoryImpl
    @Inject
    constructor(
        private val driveApi: DriveApi,
        private val sheetsApi: SpreadsheetsApi,
        private val companyDao: CompanyDao,
    ) : OnboardingRepository {
        private val _stepProgress = MutableSharedFlow<WorkspaceStep>(replay = 1)
        override val stepProgress: Flow<WorkspaceStep> = _stepProgress.asSharedFlow()

        override suspend fun createWorkspace(request: WorkspaceSetupRequest): WorkspaceCreationResult {
            val companyId = UUID.randomUUID().toString()
            var currentStep = WorkspaceStep.CREATE_FOLDER

            try {
                // Step 1: Create main folder + company folder
                currentStep = WorkspaceStep.CREATE_FOLDER
                _stepProgress.emit(currentStep)
                val mainFolder =
                    driveApi.createMainFolder()
                        ?: return WorkspaceCreationResult.Error(currentStep, "Failed to create main folder")
                val companyFolder =
                    driveApi.createCompanyFolder(mainFolder.id, request.companyName)
                        ?: return WorkspaceCreationResult.Error(currentStep, "Failed to create company folder")

                // Step 2: Upload logo (conditional — skip if no file provided)
                var logoUrl: String? = null
                if (request.logoFile != null) {
                    currentStep = WorkspaceStep.UPLOAD_LOGO
                    _stepProgress.emit(currentStep)
                    val mimeType =
                        when (request.logoFile.extension.lowercase()) {
                            "png" -> "image/png"
                            else -> "image/jpeg"
                        }
                    val fileId =
                        driveApi.uploadFile(
                            file = request.logoFile,
                            mimeType = mimeType,
                            parentFolderId = companyFolder.id,
                            fileName = "logo.${request.logoFile.extension}",
                        ) ?: return WorkspaceCreationResult.Error(currentStep, "Failed to upload logo")
                    driveApi.makeFilePublic(fileId)
                    logoUrl = "https://drive.google.com/uc?id=$fileId"
                }

                // Step 3: Create private spreadsheet (tabs: Info, Products, Categories, Pedidos)
                currentStep = WorkspaceStep.CREATE_PRIVATE_SHEET
                _stepProgress.emit(currentStep)
                val privateSheet =
                    driveApi.createPrivateSpreadsheet(companyFolder.id, request.companyName)
                        ?: return WorkspaceCreationResult.Error(currentStep, "Failed to create private spreadsheet")
                driveApi.initializeSheetHeaders(
                    privateSheet.spreadsheetId,
                    "Products",
                    listOf("ProductID", "Name", "Description", "Price", "CategoryID", "CategoryName", "Publico", "ImageUrl"),
                )
                // Hide ProductID (A=0) and CategoryID (E=4) columns
                sheetsApi.hideColumns(privateSheet.spreadsheetId, "Products", listOf(0, 4))

                driveApi.initializeSheetHeaders(
                    privateSheet.spreadsheetId,
                    "Categories",
                    listOf("CategoryID", "Name", "ProductCount", "IconEmoji"),
                )
                // Hide CategoryID (A=0) column
                sheetsApi.hideColumns(privateSheet.spreadsheetId, "Categories", listOf(0))

                val fullWhatsappNumber = "${request.whatsappCountryCode}${request.whatsappNumber}"
                val privateInfoData =
                    listOf(
                        listOf("company_id", companyId),
                        listOf("name", request.companyName),
                        listOf("specialization", request.specialization ?: ""),
                        listOf("whatsapp_number", fullWhatsappNumber),
                        listOf("logo_url", logoUrl ?: ""),
                        listOf("address", request.address ?: ""),
                        listOf("business_hours", request.businessHours ?: ""),
                    )
                if (!driveApi.writeInfoTab(privateSheet.spreadsheetId, privateInfoData)) {
                    return WorkspaceCreationResult.Error(currentStep, "Failed to write private Info tab")
                }

                // Step 4: Create public spreadsheet (tabs: Info, Products) + "anyone with link can view"
                currentStep = WorkspaceStep.CREATE_PUBLIC_SHEET
                _stepProgress.emit(currentStep)
                val publicSheet =
                    driveApi.createPublicSpreadsheet(companyFolder.id, request.companyName)
                        ?: return WorkspaceCreationResult.Error(currentStep, "Failed to create public spreadsheet")
                driveApi.initializeSheetHeaders(
                    publicSheet.spreadsheetId,
                    "Products",
                    listOf("Name", "Description", "Price", "Category", "ImageUrl"),
                )
                val publicInfoData =
                    listOf(
                        listOf("name", request.companyName),
                        listOf("specialization", request.specialization ?: ""),
                        listOf("whatsapp_number", fullWhatsappNumber),
                        listOf("logo_url", logoUrl ?: ""),
                        listOf("address", request.address ?: ""),
                        listOf("business_hours", request.businessHours ?: ""),
                    )
                if (!driveApi.writeInfoTab(publicSheet.spreadsheetId, publicInfoData)) {
                    return WorkspaceCreationResult.Error(currentStep, "Failed to write public Info tab")
                }

                // Step 5: Create images folder inside company folder
                currentStep = WorkspaceStep.CREATE_IMAGES_FOLDER
                _stepProgress.emit(currentStep)
                driveApi.createCompanyFolder(companyFolder.id, "Images")
                    ?: return WorkspaceCreationResult.Error(currentStep, "Failed to create images folder")

                // Step 6: Save company to Room
                currentStep = WorkspaceStep.SAVE_CONFIG
                _stepProgress.emit(currentStep)
                val company =
                    Company(
                        id = companyId,
                        name = request.companyName,
                        isOwned = true,
                        selected = true,
                        lastVisited = System.currentTimeMillis(),
                        whatsappNumber = request.whatsappNumber,
                        whatsappCountryCode = request.whatsappCountryCode,
                        address = request.address,
                        businessHours = request.businessHours,
                        specialization = request.specialization,
                        logoUrl = logoUrl,
                        privateSheetId = privateSheet.spreadsheetId,
                        publicSheetId = publicSheet.spreadsheetId,
                        driveFolderId = companyFolder.id,
                    )
                companyDao.unselectAllCompanies()
                companyDao.insertCompany(company)

                return WorkspaceCreationResult.Success(companyId)
            } catch (e: Exception) {
                return WorkspaceCreationResult.Error(
                    currentStep,
                    e.message ?: "Unknown error during ${currentStep.name}",
                )
            }
        }

        override suspend fun saveCompanyToRoom(company: Company) {
            companyDao.insertCompany(company)
        }

        override suspend fun getOwnedCompanyCount(): Int = companyDao.getOwnedCompanyCount()

        override suspend fun getOwnedCompanies(): List<Company> = companyDao.getOwnedCompaniesList()

        override suspend fun validateExistingWorkspace(): WorkspaceValidationResult {
            try {
                val company =
                    companyDao.getSelectedOwnedCompany()
                        ?: return WorkspaceValidationResult.NoCompany

                // Offline-fast: if Room already has sheet IDs, trust them
                if (company.privateSheetId != null && company.publicSheetId != null) {
                    return WorkspaceValidationResult.Valid(company.id)
                }

                val folderId = company.driveFolderId ?: return WorkspaceValidationResult.MissingSheets(company)

                val privateSheet = driveApi.findSpreadsheetInFolder(folderId, "Privado")
                val publicSheet = driveApi.findSpreadsheetInFolder(folderId, "Público")

                return if (privateSheet != null && publicSheet != null) {
                    companyDao.update(
                        company.copy(
                            privateSheetId = privateSheet.id,
                            publicSheetId = publicSheet.id,
                        ),
                    )
                    WorkspaceValidationResult.Valid(company.id)
                } else {
                    WorkspaceValidationResult.MissingSheets(company)
                }
            } catch (e: java.net.UnknownHostException) {
                return WorkspaceValidationResult.Error(
                    "Sin conexión a internet. Verificá tu conexión e intentá nuevamente.",
                )
            } catch (e: Exception) {
                return WorkspaceValidationResult.Error(
                    e.message ?: "Error validating workspace",
                )
            }
        }

        override suspend fun syncCompaniesFromDrive(): List<Company> {
            val mainFolder = driveApi.findMainFolder() ?: return emptyList()
            val driveFolders = driveApi.listFoldersInFolder(mainFolder.id) ?: return emptyList()

            val existingCompanies = companyDao.getOwnedCompaniesList()

            driveFolders.forEach { folder ->
                val existing = existingCompanies.find { it.driveFolderId == folder.id }
                if (existing != null) {
                    // Update name only if selected (avoid multi-device conflicts)
                    if (existing.selected && existing.name != folder.name) {
                        companyDao.update(existing.copy(name = folder.name))
                    }
                } else {
                    // New company from Drive — insert as owned, not selected
                    companyDao.insertCompany(
                        Company(
                            id = folder.id,
                            name = folder.name,
                            isOwned = true,
                            selected = false,
                            driveFolderId = folder.id,
                        ),
                    )
                }
            }

            return companyDao.getOwnedCompaniesList()
        }

        override suspend fun selectCompany(company: Company) {
            companyDao.unselectAllCompanies()
            companyDao.update(company.copy(selected = true))
        }

        override suspend fun deleteCompany(company: Company) {
            company.driveFolderId?.let { folderId ->
                driveApi.deleteFile(folderId)
            }
            companyDao.delete(company)
        }

        override suspend fun createSpreadsheetsForCompany(company: Company): WorkspaceCreationResult {
            val folderId =
                company.driveFolderId
                    ?: return WorkspaceCreationResult.Error(WorkspaceStep.CREATE_FOLDER, "Company has no Drive folder")
            var currentStep = WorkspaceStep.CREATE_PRIVATE_SHEET

            try {
                // Step 1: Create private spreadsheet
                currentStep = WorkspaceStep.CREATE_PRIVATE_SHEET
                _stepProgress.emit(currentStep)
                val privateSheet =
                    driveApi.createPrivateSpreadsheet(folderId, company.name)
                        ?: return WorkspaceCreationResult.Error(currentStep, "Failed to create private spreadsheet")
                driveApi.initializeSheetHeaders(
                    privateSheet.spreadsheetId,
                    "Products",
                    listOf("ProductID", "Name", "Description", "Price", "CategoryID", "CategoryName", "Publico", "ImageUrl"),
                )
                // Hide ProductID (A=0) and CategoryID (E=4) columns
                sheetsApi.hideColumns(privateSheet.spreadsheetId, "Products", listOf(0, 4))

                driveApi.initializeSheetHeaders(
                    privateSheet.spreadsheetId,
                    "Categories",
                    listOf("CategoryID", "Name", "ProductCount", "IconEmoji"),
                )
                // Hide CategoryID (A=0) column
                sheetsApi.hideColumns(privateSheet.spreadsheetId, "Categories", listOf(0))

                val whatsapp = "${company.whatsappCountryCode}${company.whatsappNumber ?: ""}"
                val privateInfoData =
                    listOf(
                        listOf("company_id", company.id),
                        listOf("name", company.name),
                        listOf("specialization", company.specialization ?: ""),
                        listOf("whatsapp_number", whatsapp),
                        listOf("logo_url", company.logoUrl ?: ""),
                        listOf("address", company.address ?: ""),
                        listOf("business_hours", company.businessHours ?: ""),
                    )
                driveApi.writeInfoTab(privateSheet.spreadsheetId, privateInfoData)

                // Step 2: Create public spreadsheet
                currentStep = WorkspaceStep.CREATE_PUBLIC_SHEET
                _stepProgress.emit(currentStep)
                val publicSheet =
                    driveApi.createPublicSpreadsheet(folderId, company.name)
                        ?: return WorkspaceCreationResult.Error(currentStep, "Failed to create public spreadsheet")
                driveApi.initializeSheetHeaders(
                    publicSheet.spreadsheetId,
                    "Products",
                    listOf("Name", "Description", "Price", "Category", "ImageUrl"),
                )
                val publicInfoData =
                    listOf(
                        listOf("name", company.name),
                        listOf("specialization", company.specialization ?: ""),
                        listOf("whatsapp_number", whatsapp),
                        listOf("logo_url", company.logoUrl ?: ""),
                        listOf("address", company.address ?: ""),
                        listOf("business_hours", company.businessHours ?: ""),
                    )
                driveApi.writeInfoTab(publicSheet.spreadsheetId, publicInfoData)

                // Step 3: Update company in Room with sheet IDs
                currentStep = WorkspaceStep.SAVE_CONFIG
                _stepProgress.emit(currentStep)
                companyDao.update(
                    company.copy(
                        privateSheetId = privateSheet.spreadsheetId,
                        publicSheetId = publicSheet.spreadsheetId,
                    ),
                )

                return WorkspaceCreationResult.Success(company.id)
            } catch (e: Exception) {
                return WorkspaceCreationResult.Error(
                    currentStep,
                    e.message ?: "Unknown error during ${currentStep.name}",
                )
            }
        }
    }
