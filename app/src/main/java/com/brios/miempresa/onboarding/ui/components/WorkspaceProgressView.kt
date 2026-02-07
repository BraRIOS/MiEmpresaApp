package com.brios.miempresa.onboarding.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.SuccessGreen
import com.brios.miempresa.onboarding.domain.WorkspaceStep
import com.brios.miempresa.onboarding.ui.OnboardingUiState

private const val SECONDS_PER_STEP = 3
private const val PENDING_OPACITY = 0.5f

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
    val totalSteps = steps.size
    val remainingSeconds = (totalSteps - state.completedSteps) * SECONDS_PER_STEP

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimensions.largePadding),
    ) {
        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

        // Hero text
        Text(
            text = stringResource(R.string.onboarding_step2_title_line1),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.onboarding_step2_title_line2),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

        Text(
            text = stringResource(R.string.onboarding_step2_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))

        // Progress card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimensions.OnboardingProgress.progressCardCornerRadius),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.OnboardingProgress.progressCardPadding),
            ) {
                // Top row: "EN PROGRESO" badge + "~15s restantes"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.workspace_in_progress),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier =
                            Modifier
                                .background(
                                    color = SuccessGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
                                )
                                .padding(
                                    horizontal = AppDimensions.smallPadding,
                                    vertical = AppDimensions.extraSmallPadding,
                                ),
                    )
                    Text(
                        text = stringResource(R.string.progress_remaining, remainingSeconds),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.mediumSmallPadding))

                // Large percentage
                Row(
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "${state.progressPercent}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = AppDimensions.smallPadding),
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

                // Current step label inside card
                val currentStepIndex = steps.indexOfFirst { it.name == state.currentStep }
                if (currentStepIndex >= 0) {
                    Text(
                        text =
                            stringResource(
                                R.string.step_label,
                                currentStepIndex + 1,
                                totalSteps,
                                getStepLabel(steps[currentStepIndex]),
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.mediumSmallPadding))

                // Progress bar
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(AppDimensions.OnboardingProgress.progressBarHeight)
                            .clip(MaterialTheme.shapes.small),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

        // Steps timeline
        Column {
            steps.forEachIndexed { index, step ->
                val stepStatus = getStepStatus(step, state)
                val stepLabel = getStepLabel(step)
                val isLast = index == steps.lastIndex

                StepTimelineItem(
                    label = stepLabel,
                    status = stepStatus,
                    isLast = isLast,
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))
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
private fun getStepLabel(step: WorkspaceStep): String =
    when (step) {
        WorkspaceStep.CREATE_FOLDER -> stringResource(R.string.workspace_step_folder)
        WorkspaceStep.UPLOAD_LOGO -> stringResource(R.string.workspace_step_logo)
        WorkspaceStep.CREATE_PRIVATE_SHEET -> stringResource(R.string.workspace_step_private_sheet)
        WorkspaceStep.CREATE_PUBLIC_SHEET -> stringResource(R.string.workspace_step_public_sheet)
        WorkspaceStep.CREATE_IMAGES_FOLDER -> stringResource(R.string.workspace_step_images_folder)
        WorkspaceStep.SAVE_CONFIG -> stringResource(R.string.workspace_step_save_config)
    }

@Composable
private fun StepTimelineItem(
    label: String,
    status: StepStatus,
    isLast: Boolean,
) {
    val circleSize = AppDimensions.OnboardingProgress.stepCircleSize
    val spacing = AppDimensions.OnboardingProgress.stepVerticalSpacing

    Row(
        modifier =
            Modifier.height(
                if (isLast) circleSize else circleSize + (spacing - circleSize),
            ),
    ) {
        // Circle + connector column
        Box(
            modifier = Modifier.width(circleSize),
            contentAlignment = Alignment.TopCenter,
        ) {
            // Vertical connector line (below circle)
            if (!isLast) {
                val lineColor =
                    when (status) {
                        StepStatus.COMPLETED -> SuccessGreen.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                Canvas(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = circleSize),
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = AppDimensions.OnboardingProgress.connectorLineWidth.toPx(),
                        pathEffect =
                            if (status == StepStatus.PENDING) {
                                PathEffect.dashPathEffect(
                                    floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
                                )
                            } else {
                                null
                            },
                    )
                }
            }

            // Circle indicator
            when (status) {
                StepStatus.COMPLETED -> {
                    Box(
                        modifier =
                            Modifier
                                .size(circleSize)
                                .background(color = SuccessGreen, shape = CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(R.string.workspace_step_completed),
                            tint = Color.White,
                            modifier = Modifier.size(AppDimensions.OnboardingProgress.stepCheckIconSize),
                        )
                    }
                }
                StepStatus.ACTIVE -> {
                    Box(
                        modifier =
                            Modifier
                                .size(circleSize)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(AppDimensions.OnboardingProgress.stepSpinnerIconSize),
                            strokeWidth = AppDimensions.OnboardingProgress.connectorLineWidth,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                StepStatus.PENDING -> {
                    val outlineColor = MaterialTheme.colorScheme.outlineVariant
                    val dotColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    Canvas(modifier = Modifier.size(circleSize)) {
                        drawCircle(
                            color = outlineColor,
                            radius = size.minDimension / 2f - 1.dp.toPx(),
                            style =
                                androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = AppDimensions.OnboardingProgress.connectorLineWidth.toPx(),
                                    pathEffect =
                                        PathEffect.dashPathEffect(
                                            floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
                                        ),
                                ),
                        )
                        drawCircle(
                            color = dotColor,
                            radius = AppDimensions.OnboardingProgress.stepPendingDotSize.toPx() / 2f,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(AppDimensions.OnboardingProgress.stepCircleToTextGap))

        // Label area
        when (status) {
            StepStatus.ACTIVE -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(AppDimensions.mediumPadding),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                            modifier = Modifier.padding(top = AppDimensions.extraSmallPadding),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(AppDimensions.smallPadding)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape,
                                        ),
                            )
                            Text(
                                text = stringResource(R.string.workspace_step_processing),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            StepStatus.COMPLETED -> {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough,
                    modifier = Modifier.padding(top = AppDimensions.mediumSmallPadding),
                )
            }
            StepStatus.PENDING -> {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .padding(top = AppDimensions.mediumSmallPadding)
                            .alpha(PENDING_OPACITY),
                )
            }
        }
    }
}
