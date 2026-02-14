package com.brios.miempresa.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.onboarding.ui.components.CompanySelectionView
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
    onNavigateBack: (() -> Unit)? = null,
    onSignOutRequested: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.NavigateToHome -> onNavigateToHome()
                is OnboardingEvent.NavigateBack -> onNavigateBack?.invoke()
                is OnboardingEvent.ShowError -> { /* Snackbar if needed */ }
                is OnboardingEvent.SignOutRequested -> onSignOutRequested()
            }
        }
    }

    AnimatedContent(
        targetState = uiState,
        transitionSpec = {
            if (targetState.isLoading || initialState.isLoading) {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
            } else {
                val direction =
                    if (targetState.order > initialState.order) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }

                slideIntoContainer(
                    towards = direction,
                    animationSpec = tween(500),
                ) togetherWith
                    slideOutOfContainer(
                        towards = direction,
                        animationSpec = tween(500),
                    )
            }
        },
        label = "Onboarding Transition",
        contentKey = { state ->
            when (state) {
                // All loading-like states share a single key → no animation between them
                is OnboardingUiState.Loading,
                is OnboardingUiState.DiscoveringWorkspace,
                is OnboardingUiState.ValidatingWorkspace -> "loading"
                else -> state::class
            }
        },
    ) { state ->
        when (state) {
            is OnboardingUiState.Loading -> LoadingView()
            is OnboardingUiState.DiscoveringWorkspace -> LoadingView()
            is OnboardingUiState.ValidatingWorkspace -> LoadingView()
            is OnboardingUiState.CompanySelector ->
                CompanySelectionView(
                    username = state.username,
                    companies = state.companies,
                    onSelectCompany = viewModel::selectCompany,
                    onCreateNewCompany = viewModel::createNewCompany,
                    onBack = if (viewModel.isNavigatedFromHome) onNavigateBack else null,
                )
            is OnboardingUiState.WorkspaceIssue ->
                SpreadsheetNotFoundView(
                    company = state.company,
                    onRetry = { viewModel.retryValidation() },
                    onCreateSpreadsheet = { viewModel.createSpreadsheetsForExisting(state.company) },
                    onDeleteCompany = viewModel::deleteCompany,
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
                    onCancel = viewModel::cancelWizard,
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
}

private val OnboardingUiState.order: Int
    get() =
        when (this) {
            is OnboardingUiState.Loading -> 0
            is OnboardingUiState.DiscoveringWorkspace -> 0
            is OnboardingUiState.ValidatingWorkspace -> 0
            is OnboardingUiState.Error -> 0
            is OnboardingUiState.CompanySelector -> 1
            is OnboardingUiState.WorkspaceIssue -> 1
            is OnboardingUiState.WizardStep1 -> 2
            is OnboardingUiState.WizardStep2 -> 3
            is OnboardingUiState.WizardStep3 -> 4
        }

private val OnboardingUiState.isLoading: Boolean
    get() =
        this is OnboardingUiState.Loading ||
            this is OnboardingUiState.DiscoveringWorkspace ||
            this is OnboardingUiState.ValidatingWorkspace ||
            this is OnboardingUiState.Error
