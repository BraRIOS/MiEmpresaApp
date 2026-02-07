package com.brios.miempresa.onboarding.ui

data class OnboardingFormState(
    val companyName: String = "",
    val whatsappCountryCode: String = "+54",
    val whatsappNumber: String = "",
    val specialization: String = "",
    val logoUri: String? = null,
    val address: String = "",
    val businessHours: String = "",
    val companyNameError: String? = null,
    val whatsappError: String? = null,
) {
    val isFormValid: Boolean
        get() = companyName.isNotBlank() && whatsappNumber.matches(Regex("^\\d{6,15}$"))
}

sealed class OnboardingUiState {
    data object Loading : OnboardingUiState()

    data class WizardStep1(
        val form: OnboardingFormState,
    ) : OnboardingUiState()

    data class WizardStep2(
        val completedSteps: Int,
        val currentStep: String,
        val totalSteps: Int,
        val hasLogo: Boolean,
        val errorMessage: String? = null,
    ) : OnboardingUiState() {
        val progress: Float
            get() = if (totalSteps > 0) completedSteps.toFloat() / totalSteps else 0f

        val progressPercent: Int
            get() = (progress * 100).toInt()
    }

    data class WizardStep3(
        val companyName: String,
    ) : OnboardingUiState()

    data class Error(
        val message: String,
    ) : OnboardingUiState()
}

sealed class OnboardingEvent {
    data class ShowError(val message: String) : OnboardingEvent()

    data object NavigateToHome : OnboardingEvent()
}
