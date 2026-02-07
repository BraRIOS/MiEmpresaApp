package com.brios.miempresa.onboarding.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.SuccessGreen
import com.brios.miempresa.onboarding.domain.WorkspaceStep
import com.brios.miempresa.onboarding.ui.OnboardingUiState

@Composable
fun WorkspaceProgressView(
    state: OnboardingUiState.WizardStep2,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val steps =
        if (state.hasLogo) {
            WorkspaceStep.entries
        } else {
            WorkspaceStep.entries.filter { it != WorkspaceStep.UPLOAD_LOGO }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimensions.mediumPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.largePadding),
    ) {
        // Title
        Text(
            text = stringResource(R.string.workspace_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        // Progress card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.mediumPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.workspace_in_progress),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "${state.progressPercent}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(AppDimensions.smallPadding),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        // Step list
        Column {
            steps.forEachIndexed { index, step ->
                val stepStatus = getStepStatus(step, state)
                StepItem(
                    label = getStepLabel(step),
                    status = stepStatus,
                )
                if (index < steps.lastIndex) {
                    StepConnectorLine(status = stepStatus)
                }
            }
        }
    }

    // Error dialog
    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.workspace_error_title)) },
            text = { Text(state.errorMessage) },
            confirmButton = {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.workspace_error_retry))
                }
            },
            dismissButton = {
                TextButton(onClick = {}) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

private enum class StepStatus { COMPLETED, ACTIVE, PENDING }

private fun getStepStatus(
    step: WorkspaceStep,
    state: OnboardingUiState.WizardStep2,
): StepStatus {
    val currentStep = WorkspaceStep.valueOf(state.currentStep)
    return when {
        step.displayOrder < currentStep.displayOrder -> StepStatus.COMPLETED
        step == currentStep && state.completedSteps < state.totalSteps -> StepStatus.ACTIVE
        state.completedSteps >= state.totalSteps -> StepStatus.COMPLETED
        else -> StepStatus.PENDING
    }
}

@Composable
private fun getStepLabel(step: WorkspaceStep): String {
    return when (step) {
        WorkspaceStep.CREATE_FOLDER -> stringResource(R.string.workspace_step_folder)
        WorkspaceStep.UPLOAD_LOGO -> stringResource(R.string.workspace_step_logo)
        WorkspaceStep.CREATE_PRIVATE_SHEET -> stringResource(R.string.workspace_step_private_sheet)
        WorkspaceStep.CREATE_PUBLIC_SHEET -> stringResource(R.string.workspace_step_public_sheet)
        WorkspaceStep.CREATE_IMAGES_FOLDER -> stringResource(R.string.workspace_step_images_folder)
        WorkspaceStep.SAVE_CONFIG -> stringResource(R.string.workspace_step_save_config)
    }
}

@Composable
private fun StepItem(
    label: String,
    status: StepStatus,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
        modifier = Modifier.padding(vertical = AppDimensions.extraSmallPadding),
    ) {
        // Step icon
        Box(
            modifier = Modifier.size(AppDimensions.defaultIconSize),
            contentAlignment = Alignment.Center,
        ) {
            when (status) {
                StepStatus.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = stringResource(R.string.workspace_step_completed),
                        tint = SuccessGreen,
                        modifier = Modifier.size(AppDimensions.defaultIconSize),
                    )
                }
                StepStatus.ACTIVE -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AppDimensions.mediumPadding),
                        strokeWidth = AppDimensions.mediumBorderWidth,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                StepStatus.PENDING -> {
                    val color = MaterialTheme.colorScheme.onSurfaceVariant
                    Canvas(modifier = Modifier.size(AppDimensions.defaultIconSize)) {
                        drawCircle(
                            color = color,
                            radius = size.minDimension / 2f,
                            style =
                                Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect =
                                        PathEffect.dashPathEffect(
                                            floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
                                        ),
                                ),
                        )
                    }
                }
            }
        }

        // Step text
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color =
                when (status) {
                    StepStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
                    StepStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                    StepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
        )

        // Status label for active
        if (status == StepStatus.ACTIVE) {
            Text(
                text = stringResource(R.string.workspace_step_processing),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun StepConnectorLine(status: StepStatus) {
    val color =
        when (status) {
            StepStatus.COMPLETED -> SuccessGreen
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        }

    Canvas(
        modifier =
            Modifier
                .padding(start = AppDimensions.mediumSmallPadding)
                .width(AppDimensions.smallBorderWidth)
                .height(AppDimensions.mediumPadding),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}
