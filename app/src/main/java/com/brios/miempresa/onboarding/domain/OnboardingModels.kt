package com.brios.miempresa.onboarding.domain

data class WorkspaceSetupRequest(
    val companyName: String,
    val whatsappCountryCode: String,
    val whatsappNumber: String,
    val specialization: String?,
    val logoUri: String?,
    val logoFile: java.io.File?,
    val address: String?,
    val businessHours: String?,
)

sealed class WorkspaceCreationResult {
    data class Success(val companyId: String) : WorkspaceCreationResult()

    data class Error(val step: WorkspaceStep, val message: String) : WorkspaceCreationResult()
}

enum class WorkspaceStep(val displayOrder: Int) {
    CREATE_FOLDER(1),
    UPLOAD_LOGO(2),
    CREATE_PRIVATE_SHEET(3),
    CREATE_PUBLIC_SHEET(4),
    POPULATE_INFO(5),
    CREATE_IMAGES_FOLDER(6),
    SAVE_CONFIG(7),
}

sealed class WorkspaceValidationResult {
    data class Valid(val companyId: String) : WorkspaceValidationResult()

    data object NoCompany : WorkspaceValidationResult()

    data class MissingSheets(
        val company: com.brios.miempresa.core.data.local.entities.Company,
    ) : WorkspaceValidationResult()

    data class Error(val message: String) : WorkspaceValidationResult()
}

enum class WorkspaceIssueType {
    SPREADSHEET_NOT_FOUND,
    NO_CONNECTION,
    GENERIC_ERROR,
}
