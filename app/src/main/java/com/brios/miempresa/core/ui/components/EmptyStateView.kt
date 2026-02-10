package com.brios.miempresa.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray500

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(AppDimensions.emptyStateIconSize)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        Color.White,
                                    ),
                            ),
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppDimensions.emptyStateInnerIconSize),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            )

            // Animated pulsing dot (Top-Right)
            PulsingDot(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-16).dp, y = 24.dp),
                size = 12.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            )

            // Animated pulsing dot (Bottom-Left) - Alternating phase
            PulsingDot(
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 24.dp, y = (-32).dp),
                size = 8.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                initialDelayMillis = 1000,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

        Text(
            modifier = Modifier.padding(horizontal = AppDimensions.largePadding),
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = SlateGray500,
            textAlign = TextAlign.Center,
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

            Button(
                onClick = onAction,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = AppDimensions.largePadding, vertical = AppDimensions.mediumPadding),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun PulsingDot(
    modifier: Modifier = Modifier,
    size: Dp,
    color: Color,
    initialDelayMillis: Int = 0,
) {
    val transition = rememberInfiniteTransition(label = "pulsing-dot")
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(initialDelayMillis),
            ),
        label = "dot-alpha",
    )

    Box(
        modifier =
            modifier
                .size(size)
                .alpha(alpha)
                .background(
                    color = color,
                    shape = CircleShape,
                ),
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewPreview() {
    MiEmpresaTheme {
        EmptyStateView(
            icon = Icons.Filled.ShoppingBag,
            title = "Aún no tenés productos",
            subtitle = "Empezá agregando tu primer producto al catálogo",
            actionLabel = "Agregar primer producto",
            onAction = {},
        )
    }
}
