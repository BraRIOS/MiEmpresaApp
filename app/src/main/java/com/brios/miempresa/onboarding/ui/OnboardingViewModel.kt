package com.brios.miempresa.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.onboarding.data.OnboardingRepositoryImpl
import com.brios.miempresa.onboarding.domain.WorkspaceCreationResult
import com.brios.miempresa.onboarding.domain.WorkspaceSetupRequest
import com.brios.miempresa.onboarding.domain.WorkspaceStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val repository: OnboardingRepositoryImpl,
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
            checkExistingCompanies()
        }

        private fun checkExistingCompanies() {
            viewModelScope.launch {
                try {
                    val count = repository.getOwnedCompanyCount()
                    if (count > 0) {
                        _events.emit(OnboardingEvent.NavigateToHome)
                    } else {
                        _uiState.value = OnboardingUiState.WizardStep1(formState)
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        OnboardingUiState.Error(
                            e.message ?: "Error checking existing companies",
                        )
                }
            }
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
            formState = formState.copy(logoUri = uri)
            _uiState.value = OnboardingUiState.WizardStep1(formState)
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
                        address = formState.address.ifBlank { null },
                        businessHours = formState.businessHours.ifBlank { null },
                    )

                when (val result = repository.createWorkspace(request)) {
                    is WorkspaceCreationResult.Success -> {
                        _uiState.value =
                            OnboardingUiState.WizardStep2(
                                completedSteps = totalSteps,
                                currentStep = WorkspaceStep.SAVE_CONFIG.name,
                                totalSteps = totalSteps,
                                hasLogo = hasLogo,
                            )
                        _uiState.value =
                            OnboardingUiState.WizardStep3(
                                companyName = formState.companyName.trim(),
                                whatsappCountryCode = formState.whatsappCountryCode,
                                whatsappNumber = formState.whatsappNumber.trim(),
                                logoUri = formState.logoUri,
                            )
                    }
                    is WorkspaceCreationResult.Error -> {
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
            startWorkspaceCreation()
        }

        fun navigateToHome() {
            viewModelScope.launch {
                _events.emit(OnboardingEvent.NavigateToHome)
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
