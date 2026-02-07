package com.brios.miempresa.onboarding.data

import com.brios.miempresa.core.api.drive.DriveApi
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.onboarding.domain.OnboardingRepository
import com.brios.miempresa.onboarding.domain.WorkspaceCreationResult
import com.brios.miempresa.onboarding.domain.WorkspaceSetupRequest
import com.brios.miempresa.onboarding.domain.WorkspaceStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID
import javax.inject.Inject

class OnboardingRepositoryImpl
    @Inject
    constructor(
        private val driveApi: DriveApi,
        private val companyDao: CompanyDao,
    ) : OnboardingRepository {
        // TODO Sprint 3: Implement workspace validation for returning users (initializer-logic-extraction §2.1)
        // - findMainFolder() → listFoldersInFolder() → findSpreadsheetInFolder()
        // - Handle SpreadsheetNotFound with 4 recovery options (Retry/Create/Delete/SelectAnother)

        // TODO Sprint 3: Implement Drive ↔ Room company sync for multi-device (initializer-logic-extraction §2.2)
        // - Compare Drive folders with Room companies, insert new (selected=false), update name for selected only

        // TODO Sprint 3: Distinguish Drive API error types (initializer-logic-extraction §4.1)
        // - NotFound vs AuthError (UserRecoverableAuthIOException) vs NetworkError (currently all generic)

        // TODO Sprint 3: Validate sheet structure after finding (initializer-logic-extraction §7.2)
        // - Check Productos + Categorías sheets exist with correct headers

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

                // Step 2: Upload logo (optional, skip if no URI provided)
                currentStep = WorkspaceStep.UPLOAD_LOGO
                _stepProgress.emit(currentStep)
                // TODO: Implement logo upload when DriveApi supports file upload

                // Step 3: Create private spreadsheet (tabs: Info, Products, Categories, Pedidos)
                currentStep = WorkspaceStep.CREATE_PRIVATE_SHEET
                _stepProgress.emit(currentStep)
                val privateSheet =
                    driveApi.createPrivateSpreadsheet(companyFolder.id, request.companyName)
                        ?: return WorkspaceCreationResult.Error(currentStep, "Failed to create private spreadsheet")
                driveApi.initializeSheetHeaders(
                    privateSheet.spreadsheetId,
                    "Products",
                    listOf("Name", "Description", "Price", "Category", "ImageId"),
                )
                driveApi.initializeSheetHeaders(
                    privateSheet.spreadsheetId,
                    "Categories",
                    listOf("Name", "Icon", "ProductCount"),
                )

                // Step 4: Create public spreadsheet (tabs: Info, Products) + "anyone with link can view"
                currentStep = WorkspaceStep.CREATE_PUBLIC_SHEET
                _stepProgress.emit(currentStep)
                val publicSheet =
                    driveApi.createPublicSpreadsheet(companyFolder.id, request.companyName)
                        ?: return WorkspaceCreationResult.Error(currentStep, "Failed to create public spreadsheet")
                driveApi.initializeSheetHeaders(
                    publicSheet.spreadsheetId,
                    "Products",
                    listOf("Name", "Description", "Price", "Category", "ImageId"),
                )

                // Step 5: Populate Tab "Info" in both sheets
                currentStep = WorkspaceStep.POPULATE_INFO
                _stepProgress.emit(currentStep)
                val fullWhatsappNumber = "${request.whatsappCountryCode}${request.whatsappNumber}"
                val privateInfoData =
                    listOf(
                        listOf("company_id", companyId),
                        listOf("name", request.companyName),
                        listOf("specialization", request.specialization ?: ""),
                        listOf("whatsapp_number", fullWhatsappNumber),
                        listOf("logo_url", request.logoUri ?: ""),
                        listOf("address", request.address ?: ""),
                        listOf("business_hours", request.businessHours ?: ""),
                    )
                if (!driveApi.writeInfoTab(privateSheet.spreadsheetId, privateInfoData)) {
                    return WorkspaceCreationResult.Error(currentStep, "Failed to write private Info tab")
                }
                val publicInfoData =
                    listOf(
                        listOf("name", request.companyName),
                        listOf("specialization", request.specialization ?: ""),
                        listOf("whatsapp_number", fullWhatsappNumber),
                        listOf("logo_url", request.logoUri ?: ""),
                        listOf("address", request.address ?: ""),
                        listOf("business_hours", request.businessHours ?: ""),
                    )
                if (!driveApi.writeInfoTab(publicSheet.spreadsheetId, publicInfoData)) {
                    return WorkspaceCreationResult.Error(currentStep, "Failed to write public Info tab")
                }

                // Step 6: Create images folder inside company folder
                currentStep = WorkspaceStep.CREATE_IMAGES_FOLDER
                _stepProgress.emit(currentStep)
                driveApi.createCompanyFolder(companyFolder.id, "Images")
                    ?: return WorkspaceCreationResult.Error(currentStep, "Failed to create images folder")

                // Step 7: Save company to Room
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
                        logoUrl = request.logoUri,
                        privateSheetId = privateSheet.spreadsheetId,
                        publicSheetId = publicSheet.spreadsheetId,
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
    }
