package com.brios.miempresa.onboarding.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.onboarding.ui.components.WorkspaceProgressView

/**
 * Temporary Activity for visual testing of WorkspaceProgressView states via adb.
 * Launch with state index (0-3):
 *   adb shell am start -n com.brios.miempresa/.onboarding.ui.WorkspacePreviewActivity --ei state 0
 *   adb shell am start -n com.brios.miempresa/.onboarding.ui.WorkspacePreviewActivity --ei state 1
 *   adb shell am start -n com.brios.miempresa/.onboarding.ui.WorkspacePreviewActivity --ei state 2
 *   adb shell am start -n com.brios.miempresa/.onboarding.ui.WorkspacePreviewActivity --ei state 3
 * DELETE before release.
 */
class WorkspacePreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val states =
            listOf(
                OnboardingUiState.WizardStep2(
                    completedSteps = 0,
                    totalSteps = 7,
                    currentStep = "CREATE_FOLDER",
                    errorMessage = null,
                    hasLogo = true,
                ),
                OnboardingUiState.WizardStep2(
                    completedSteps = 3,
                    totalSteps = 7,
                    currentStep = "CREATE_PUBLIC_SHEET",
                    errorMessage = null,
                    hasLogo = true,
                ),
                OnboardingUiState.WizardStep2(
                    completedSteps = 5,
                    totalSteps = 6,
                    currentStep = "SAVE_CONFIG",
                    errorMessage = null,
                    hasLogo = false,
                ),
                OnboardingUiState.WizardStep2(
                    completedSteps = 2,
                    totalSteps = 7,
                    currentStep = "UPLOAD_LOGO",
                    errorMessage = "No se pudo subir el logo. Verificá tu conexión a internet.",
                    hasLogo = true,
                ),
            )

        val stateIndex = intent.getIntExtra("state", 0).coerceIn(0, states.lastIndex)

        setContent {
            MiEmpresaTheme {
                WorkspaceProgressView(
                    state = states[stateIndex],
                    onRetry = {},
                )
            }
        }
    }
}
