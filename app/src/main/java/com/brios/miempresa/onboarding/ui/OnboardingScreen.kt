package com.brios.miempresa.onboarding.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.core.ui.components.CompanyListView
import com.brios.miempresa.core.ui.components.LoadingView
import com.brios.miempresa.core.ui.components.MessageWithIcon
import com.brios.miempresa.core.ui.components.SpreadsheetNotFoundView
import com.brios.miempresa.onboarding.ui.components.CompanyFormStep
import com.brios.miempresa.onboarding.ui.components.OnboardingSuccessView
import com.brios.miempresa.onboarding.ui.components.WorkspaceProgressView

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.NavigateToHome -> onNavigateToHome()
                is OnboardingEvent.ShowError -> { /* Snackbar if needed */ }
                is OnboardingEvent.NavigateToCompanySelector -> { /* Handled via uiState */ }
            }
        }
    }

    when (val state = uiState) {
        is OnboardingUiState.Loading -> LoadingView()
        is OnboardingUiState.ValidatingWorkspace -> LoadingView(message = "Validando workspace...")
        is OnboardingUiState.CompanySelector ->
            CompanyListView(
                username = state.username,
                companies = state.companies,
                onSelectCompany = viewModel::selectCompany,
                onCreateNewCompany = viewModel::createNewCompany,
            )
        is OnboardingUiState.WorkspaceIssue ->
            SpreadsheetNotFoundView(
                company = state.company,
                onRetry = { viewModel.retryValidation() },
                onCreateSpreadsheet = { viewModel.createSpreadsheetsForExisting(state.company) },
                onDeleteCompany = viewModel::deleteLocalCompany,
                onSelectAnotherCompany = viewModel::showSelector,
            )
        is OnboardingUiState.WizardStep1 ->
            CompanyFormStep(
                form = state.form,
                onUpdateName = viewModel::updateCompanyName,
                onUpdateCountryCode = viewModel::updateWhatsappCountryCode,
                onUpdateWhatsapp = viewModel::updateWhatsappNumber,
                onUpdateSpecialization = viewModel::updateSpecialization,
                onUpdateLogoUri = viewModel::updateLogoUri,
                onUpdateAddress = viewModel::updateAddress,
                onUpdateBusinessHours = viewModel::updateBusinessHours,
                onContinue = viewModel::startWorkspaceCreation,
                onCancel = onNavigateBack,
            )
        is OnboardingUiState.WizardStep2 ->
            WorkspaceProgressView(
                state = state,
                onRetry = viewModel::retryWorkspaceCreation,
            )
        is OnboardingUiState.WizardStep3 ->
            OnboardingSuccessView(
                companyName = state.companyName,
                whatsappCountryCode = state.whatsappCountryCode,
                whatsappNumber = state.whatsappNumber,
                logoUri = state.logoUri,
                onNavigateToHome = viewModel::navigateToHome,
            )
        is OnboardingUiState.Error -> {
            MessageWithIcon(
                message = state.message,
                icon = Icons.Outlined.ErrorOutline,
            )
        }
    }
}
