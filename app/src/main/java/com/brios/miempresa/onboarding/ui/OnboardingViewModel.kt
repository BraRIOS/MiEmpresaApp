package com.brios.miempresa.onboarding.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.core.auth.GoogleAuthClient
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.onboarding.domain.OnboardingRepository
import com.brios.miempresa.onboarding.domain.WorkspaceCreationResult
import com.brios.miempresa.onboarding.domain.WorkspaceIssueType
import com.brios.miempresa.onboarding.domain.WorkspaceSetupRequest
import com.brios.miempresa.onboarding.domain.WorkspaceStep
import com.brios.miempresa.onboarding.domain.WorkspaceValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
        private val repository: OnboardingRepository,
        private val googleAuthClient: GoogleAuthClient,
    ) : ViewModel() {
        companion object {
            const val MAX_COMPANY_NAME = 50
            const val MAX_WHATSAPP_NUMBER = 15
            const val MAX_SPECIALIZATION = 30
            const val MAX_ADDRESS = 100
            const val MAX_BUSINESS_HOURS = 50
        }

        private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Loading)
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        private val _events = MutableSharedFlow<OnboardingEvent>()
        val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

        private var formState = OnboardingFormState()

        init {
            initializeOnboarding()
        }

        private fun initializeOnboarding() {
            viewModelScope.launch {
                try {
                    // Offline-first: check Room for a selected company with valid sheets
                    val localCompanies = repository.getOwnedCompanies()
                    val selectedCompany = localCompanies.find { it.selected }

                    if (selectedCompany != null &&
                        selectedCompany.privateSheetId != null &&
                        selectedCompany.publicSheetId != null
                    ) {
                        // Fast path: Room has valid company → go home immediately
                        _events.emit(OnboardingEvent.NavigateToHome)
                        return@launch
                    }

                    // Room doesn't have a fully valid workspace — discover from Drive
                    _uiState.value = OnboardingUiState.DiscoveringWorkspace("Buscando tu espacio de trabajo...")
                    val companies =
                        try {
                            repository.syncCompaniesFromDrive()
                        } catch (_: Exception) {
                            // Drive unavailable — use whatever Room has
                            localCompanies
                        }

                    if (companies.isEmpty()) {
                        _uiState.value = OnboardingUiState.WizardStep1(formState)
                        return@launch
                    }

                    // Route based on company count and selection
                    val selectedExists = companies.any { it.selected }
                    if (!selectedExists && companies.size > 1) {
                        showCompanySelector(companies)
                        return@launch
                    }

                    if (!selectedExists && companies.size == 1) {
                        repository.selectCompany(companies.first())
                    }

                    // Validate workspace for selected company
                    _uiState.value = OnboardingUiState.DiscoveringWorkspace("Verificando base de datos...")
                    when (val result = repository.validateExistingWorkspace()) {
                        is WorkspaceValidationResult.Valid ->
                            _events.emit(OnboardingEvent.NavigateToHome)
                        is WorkspaceValidationResult.MissingSheets ->
                            _uiState.value =
                                OnboardingUiState.WorkspaceIssue(
                                    company = result.company,
                                    issueType = WorkspaceIssueType.SPREADSHEET_NOT_FOUND,
                                )
                        is WorkspaceValidationResult.NoCompany ->
                            _uiState.value = OnboardingUiState.WizardStep1(formState)
                        is WorkspaceValidationResult.Error ->
                            _uiState.value =
                                OnboardingUiState.WorkspaceIssue(
                                    company = Company(id = "", name = ""),
                                    issueType = WorkspaceIssueType.GENERIC_ERROR,
                                )
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        OnboardingUiState.Error(
                            e.message ?: "Error initializing onboarding",
                        )
                }
            }
        }

        private fun showCompanySelector(companies: List<Company>) {
            val username = googleAuthClient.getSignedInUser()?.username ?: ""
            _uiState.value =
                OnboardingUiState.CompanySelector(
                    companies = companies,
                    username = username,
                )
        }

        fun updateCompanyName(name: String) {
            formState =
                formState.copy(
                    companyName = name.take(MAX_COMPANY_NAME),
                    companyNameError = null,
                )
            _uiState.value = OnboardingUiState.WizardStep1(formState)
        }

        fun updateWhatsappCountryCode(code: String) {
            formState = formState.copy(whatsappCountryCode = code)
            _uiState.value = OnboardingUiState.WizardStep1(formState)
        }

        fun updateWhatsappNumber(number: String) {
            formState =
                formState.copy(
                    whatsappNumber = number.take(MAX_WHATSAPP_NUMBER),
                    whatsappError = null,
                )
            _uiState.value = OnboardingUiState.WizardStep1(formState)
        }

        fun updateSpecialization(specialization: String) {
            formState = formState.copy(specialization = specialization.take(MAX_SPECIALIZATION))
            _uiState.value = OnboardingUiState.WizardStep1(formState)
        }

        fun updateLogoUri(uri: String?) {
            if (uri != null) {
                val file = resolveUriToFile(Uri.parse(uri))
                formState = formState.copy(logoUri = uri, logoFile = file)
            } else {
                formState = formState.copy(logoUri = null, logoFile = null)
            }
            _uiState.value = OnboardingUiState.WizardStep1(formState)
        }

        private fun resolveUriToFile(uri: Uri): File? {
            return try {
                val extension =
                    appContext.contentResolver.getType(uri)?.let { mimeType ->
                        when {
                            mimeType.contains("png") -> "png"
                            else -> "jpg"
                        }
                    } ?: "jpg"
                val tempFile = File(appContext.cacheDir, "logo_upload.$extension")
                appContext.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            } catch (e: Exception) {
                null
            }
        }

        fun updateAddress(address: String) {
            formState = formState.copy(address = address.take(MAX_ADDRESS))
            _uiState.value = OnboardingUiState.WizardStep1(formState)
        }

        fun updateBusinessHours(hours: String) {
            formState = formState.copy(businessHours = hours.take(MAX_BUSINESS_HOURS))
            _uiState.value = OnboardingUiState.WizardStep1(formState)
        }

        fun startWorkspaceCreation() {
            if (!validateForm()) return

            val totalSteps = WorkspaceStep.entries.size
            val hasLogo = formState.logoUri != null

            _uiState.value =
                OnboardingUiState.WizardStep2(
                    completedSteps = 0,
                    currentStep = WorkspaceStep.CREATE_FOLDER.name,
                    totalSteps = totalSteps,
                    hasLogo = hasLogo,
                )

            viewModelScope.launch {
                val progressJob =
                    launch {
                        repository.stepProgress.collect { step ->
                            _uiState.value =
                                OnboardingUiState.WizardStep2(
                                    completedSteps = step.displayOrder - 1,
                                    currentStep = step.name,
                                    totalSteps = totalSteps,
                                    hasLogo = hasLogo,
                                )
                        }
                    }

                val request =
                    WorkspaceSetupRequest(
                        companyName = formState.companyName.trim(),
                        whatsappCountryCode = formState.whatsappCountryCode,
                        whatsappNumber = formState.whatsappNumber.trim(),
                        specialization = formState.specialization.ifBlank { null },
                        logoUri = formState.logoUri,
                        logoFile = formState.logoFile,
                        address = formState.address.ifBlank { null },
                        businessHours = formState.businessHours.ifBlank { null },
                    )

                when (val result = repository.createWorkspace(request)) {
                    is WorkspaceCreationResult.Success -> {
                        progressJob.cancel()
                        _uiState.value =
                            OnboardingUiState.WizardStep3(
                                companyName = formState.companyName.trim(),
                                whatsappCountryCode = formState.whatsappCountryCode,
                                whatsappNumber = formState.whatsappNumber.trim(),
                                logoUri = formState.logoUri,
                            )
                    }
                    is WorkspaceCreationResult.Error -> {
                        progressJob.cancel()
                        _uiState.value =
                            OnboardingUiState.WizardStep2(
                                completedSteps = result.step.displayOrder - 1,
                                currentStep = result.step.name,
                                totalSteps = totalSteps,
                                hasLogo = hasLogo,
                                errorMessage = result.message,
                            )
                        _events.emit(OnboardingEvent.ShowError(result.message))
                    }
                }
            }
        }

        fun retryWorkspaceCreation() {
            // TODO Sprint 3: Implement incremental retry that resumes from failed step
            // Currently restarts from scratch; folder creation is idempotent but
            // spreadsheet creation may leave orphaned sheets in Drive on retry.
            startWorkspaceCreation()
        }

        fun navigateToHome() {
            viewModelScope.launch {
                _events.emit(OnboardingEvent.NavigateToHome)
            }
        }

        fun selectCompany(company: Company) {
            viewModelScope.launch {
                _uiState.value = OnboardingUiState.DiscoveringWorkspace("Verificando base de datos...")
                repository.selectCompany(company)

                // Offline-fast: if company already has sheet IDs, go home
                if (company.privateSheetId != null && company.publicSheetId != null) {
                    _events.emit(OnboardingEvent.NavigateToHome)
                    return@launch
                }

                when (val result = repository.validateExistingWorkspace()) {
                    is WorkspaceValidationResult.Valid ->
                        _events.emit(OnboardingEvent.NavigateToHome)
                    is WorkspaceValidationResult.MissingSheets ->
                        _uiState.value =
                            OnboardingUiState.WorkspaceIssue(
                                company = result.company,
                                issueType = WorkspaceIssueType.SPREADSHEET_NOT_FOUND,
                            )
                    is WorkspaceValidationResult.NoCompany ->
                        _uiState.value = OnboardingUiState.WizardStep1(formState)
                    is WorkspaceValidationResult.Error ->
                        _uiState.value = OnboardingUiState.Error(result.message)
                }
            }
        }

        fun retryValidation() {
            viewModelScope.launch {
                _uiState.value = OnboardingUiState.DiscoveringWorkspace("Reintentando búsqueda...")
                when (val result = repository.validateExistingWorkspace()) {
                    is WorkspaceValidationResult.Valid ->
                        _events.emit(OnboardingEvent.NavigateToHome)
                    is WorkspaceValidationResult.MissingSheets ->
                        _uiState.value =
                            OnboardingUiState.WorkspaceIssue(
                                company = result.company,
                                issueType = WorkspaceIssueType.SPREADSHEET_NOT_FOUND,
                            )
                    is WorkspaceValidationResult.NoCompany ->
                        _uiState.value = OnboardingUiState.WizardStep1(formState)
                    is WorkspaceValidationResult.Error ->
                        _uiState.value = OnboardingUiState.Error(result.message)
                }
            }
        }

        fun createNewCompany() {
            formState = OnboardingFormState()
            _uiState.value = OnboardingUiState.WizardStep1(formState)
        }

        fun deleteCompany(company: Company) {
            viewModelScope.launch {
                _uiState.value = OnboardingUiState.DiscoveringWorkspace("Eliminando empresa...")
                repository.deleteCompany(company)
                val companies = repository.getOwnedCompanies()
                if (companies.isEmpty()) {
                    _uiState.value = OnboardingUiState.WizardStep1(formState)
                } else {
                    showCompanySelector(companies)
                }
            }
        }

        fun createSpreadsheetsForExisting(company: Company) {
            val totalSteps = WorkspaceStep.entries.size
            _uiState.value =
                OnboardingUiState.WizardStep2(
                    completedSteps = 0,
                    currentStep = WorkspaceStep.CREATE_PRIVATE_SHEET.name,
                    totalSteps = totalSteps,
                    hasLogo = false,
                )

            viewModelScope.launch {
                val progressJob =
                    launch {
                        repository.stepProgress.collect { step ->
                            _uiState.value =
                                OnboardingUiState.WizardStep2(
                                    completedSteps = step.displayOrder - 1,
                                    currentStep = step.name,
                                    totalSteps = totalSteps,
                                    hasLogo = false,
                                )
                        }
                    }

                when (val result = repository.createSpreadsheetsForCompany(company)) {
                    is WorkspaceCreationResult.Success -> {
                        progressJob.cancel()
                        _events.emit(OnboardingEvent.NavigateToHome)
                    }
                    is WorkspaceCreationResult.Error -> {
                        progressJob.cancel()
                        _uiState.value =
                            OnboardingUiState.WorkspaceIssue(
                                company = company,
                                issueType = WorkspaceIssueType.SPREADSHEET_NOT_FOUND,
                            )
                        _events.emit(OnboardingEvent.ShowError(result.message))
                    }
                }
            }
        }

        fun showSelector() {
            viewModelScope.launch {
                val companies = repository.getOwnedCompanies()
                if (companies.isEmpty()) {
                    _uiState.value = OnboardingUiState.WizardStep1(formState)
                } else {
                    showCompanySelector(companies)
                }
            }
        }

        private fun validateForm(): Boolean {
            var hasErrors = false

            if (formState.companyName.isBlank()) {
                formState = formState.copy(companyNameError = "required")
                hasErrors = true
            }

            if (!formState.whatsappNumber.matches(Regex("^\\d{6,15}$"))) {
                formState = formState.copy(whatsappError = "invalid")
                hasErrors = true
            }

            if (hasErrors) {
                _uiState.value = OnboardingUiState.WizardStep1(formState)
            }

            return !hasErrors
        }
    }
