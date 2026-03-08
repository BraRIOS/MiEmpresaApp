package com.brios.miempresa.onboarding.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.SlateGray100
import com.brios.miempresa.core.ui.theme.SuccessGreen
import com.brios.miempresa.onboarding.domain.WorkspaceStep
import com.brios.miempresa.onboarding.ui.OnboardingUiState

private const val SECONDS_PER_STEP = 3
private const val PENDING_OPACITY = 0.5f
private const val SHIMMER_DURATION_MS = 2000
private const val PING_DURATION_MS = 1500
private const val ACTIVE_CARD_SCALE = 1.02f
private const val RING_WIDTH_DP = 4
private const val CARD_BORDER_ALPHA = 0.2f

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

    Box(modifier = modifier.fillMaxSize()) {
        val dotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 25.dp.toPx()
            val radius = 1.5.dp.toPx()
            for (x in 20..size.width.toInt() step step.toInt()) {
                for (y in 20..size.height.toInt() step step.toInt()) {
                    drawCircle(
                        color = dotColor,
                        radius = radius,
                        center = Offset(x.toFloat(), y.toFloat()),
                    )
                }
            }
        }

        Column(
            modifier =
                Modifier
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = SlateGray100,
                            shape = RoundedCornerShape(AppDimensions.OnboardingProgress.progressCardCornerRadius),
                        ),
                shape = RoundedCornerShape(AppDimensions.OnboardingProgress.progressCardCornerRadius),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                elevation =
                    CardDefaults.cardElevation(defaultElevation = 12.dp),
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
                            color = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
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
                            fontWeight = FontWeight.ExtraBold,
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

                    // Progress bar with shimmer
                    ShimmerProgressBar(
                        progress = state.progress,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(AppDimensions.OnboardingProgress.progressBarHeight),
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

            // Steps timeline with connector line
            val circleColumnWidth = AppDimensions.OnboardingProgress.stepCircleSize + RING_WIDTH_DP.dp * 2
            Box {
                // Connector line behind circles
                val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                Canvas(
                    modifier =
                        Modifier
                            .width(circleColumnWidth)
                            .matchParentSize(),
                ) {
                    val centerX = (size.width - 30) / 12
                    val topOffset = circleColumnWidth.toPx() / 2
                    val bottomOffset = topOffset
                    drawLine(
                        color = lineColor,
                        start = Offset(centerX, topOffset),
                        end = Offset(centerX, size.height - bottomOffset),
                        strokeWidth = AppDimensions.OnboardingProgress.connectorLineWidth.toPx(),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                ) {
                    steps.forEachIndexed { index, step ->
                        val stepStatus = getStepStatus(step, state)
                        val stepLabel = getStepLabel(step)

                        StepTimelineItem(
                            label = stepLabel,
                            status = stepStatus,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))
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
private fun ShimmerProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(SHIMMER_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "shimmerOffset",
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val barColor = MaterialTheme.colorScheme.primary
    val shimmerBrush =
        Brush.linearGradient(
            colors =
                listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.3f),
                    Color.Transparent,
                ),
            start = Offset(shimmerOffset * 300f, 0f),
            end = Offset((shimmerOffset + 0.5f) * 300f, 0f),
        )

    Canvas(modifier = modifier.clip(RoundedCornerShape(50))) {
        // Track
        drawRoundRect(
            color = trackColor,
            size = size,
        )
        // Filled portion
        val filledWidth = size.width * progress
        if (filledWidth > 0f) {
            drawRoundRect(
                color = barColor,
                size = size.copy(width = filledWidth),
            )
            // Shimmer overlay on filled portion
            drawRect(
                brush = shimmerBrush,
                size = size.copy(width = filledWidth),
            )
        }
    }
}

@Composable
private fun StepTimelineItem(
    label: String,
    status: StepStatus,
) {
    val circleSize = AppDimensions.OnboardingProgress.stepCircleSize
    val totalCircleSize = circleSize + RING_WIDTH_DP.dp * 2
    val ringColor = MaterialTheme.colorScheme.background

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Ring + circle indicator
        Box(
            modifier = Modifier.size(totalCircleSize),
            contentAlignment = Alignment.Center,
        ) {
            // Ring background
            Box(
                modifier =
                    Modifier
                        .size(totalCircleSize)
                        .background(color = ringColor, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
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
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
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
        }

        Spacer(modifier = Modifier.width(AppDimensions.OnboardingProgress.stepCircleToTextGap - RING_WIDTH_DP.dp))

        // Label area
        when (status) {
            StepStatus.ACTIVE -> {
                val primaryColor = MaterialTheme.colorScheme.primary

                val infiniteTransition = rememberInfiniteTransition(label = "ping")
                val pingScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 2.5f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(PING_DURATION_MS, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart,
                        ),
                    label = "pingScale",
                )
                val pingAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.75f,
                    targetValue = 0f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(PING_DURATION_MS, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart,
                        ),
                    label = "pingAlpha",
                )

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .scale(ACTIVE_CARD_SCALE)
                            .border(
                                width = 1.dp,
                                color = primaryColor.copy(alpha = CARD_BORDER_ALPHA),
                                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                            ),
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(AppDimensions.mediumSmallPadding),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(AppDimensions.smallPadding)
                                            .scale(pingScale)
                                            .alpha(pingAlpha)
                                            .background(
                                                color = primaryColor,
                                                shape = CircleShape,
                                            ),
                                )
                                Box(
                                    modifier =
                                        Modifier
                                            .size(AppDimensions.smallPadding)
                                            .background(
                                                color = primaryColor,
                                                shape = CircleShape,
                                            ),
                                )
                            }
                            Text(
                                text = stringResource(R.string.workspace_step_processing),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor,
                            )
                        }
                    }
                }
            }
            StepStatus.COMPLETED -> {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough,
                )
            }
            StepStatus.PENDING -> {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(PENDING_OPACITY),
                )
            }
        }
    }
}

// ============================================================
// Previews
// ============================================================

@androidx.compose.ui.tooling.preview.Preview(
    name = "Step2 Progress 0%",
    showBackground = true,
)
@Composable
private fun PreviewWorkspaceProgress0() {
    com.brios.miempresa.core.ui.theme.MiEmpresaTheme {
        WorkspaceProgressView(
            state =
                OnboardingUiState.WizardStep2(
                    completedSteps = 0,
                    totalSteps = 7,
                    currentStep = "CREATE_FOLDER",
                    errorMessage = null,
                    hasLogo = true,
                ),
            onRetry = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Step2 Progress 42%",
    showBackground = true,
)
@Composable
private fun PreviewWorkspaceProgress42() {
    com.brios.miempresa.core.ui.theme.MiEmpresaTheme {
        WorkspaceProgressView(
            state =
                OnboardingUiState.WizardStep2(
                    completedSteps = 3,
                    totalSteps = 7,
                    currentStep = "CREATE_PUBLIC_SHEET",
                    errorMessage = null,
                    hasLogo = true,
                ),
            onRetry = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Step2 Progress 85% (No logo)",
    showBackground = true,
)
@Composable
private fun PreviewWorkspaceProgress85NoLogo() {
    com.brios.miempresa.core.ui.theme.MiEmpresaTheme {
        WorkspaceProgressView(
            state =
                OnboardingUiState.WizardStep2(
                    completedSteps = 5,
                    totalSteps = 6,
                    currentStep = "SAVE_CONFIG",
                    errorMessage = null,
                    hasLogo = false,
                ),
            onRetry = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Step2 With Error",
    showBackground = true,
)
@Composable
private fun PreviewWorkspaceProgressError() {
    com.brios.miempresa.core.ui.theme.MiEmpresaTheme {
        WorkspaceProgressView(
            state =
                OnboardingUiState.WizardStep2(
                    completedSteps = 2,
                    totalSteps = 7,
                    currentStep = "UPLOAD_LOGO",
                    errorMessage = "No se pudo subir el logo. Verificá tu conexión a internet.",
                    hasLogo = true,
                ),
            onRetry = {},
        )
    }
}
