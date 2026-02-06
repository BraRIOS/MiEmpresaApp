package com.brios.miempresa.core.ui.components

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

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
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppDimensions.largeIconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        PulsingDots()

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            FilledTonalButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
private fun PulsingDots() {
    val transition = rememberInfiniteTransition(label = "pulsing-dots")
    val delays = listOf(0, 300, 600)

    Row(
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        delays.forEach { delay ->
            PulsingDot(transition = transition, delayMillis = delay)
        }
    }
}

@Composable
private fun PulsingDot(
    transition: InfiniteTransition,
    delayMillis: Int,
) {
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 600, delayMillis = delayMillis),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "dot-alpha-$delayMillis",
    )

    Box(
        modifier =
            Modifier
                .size(AppDimensions.emptyStateDotSize)
                .alpha(alpha)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape,
                ),
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewPreview() {
    MiEmpresaTheme {
        EmptyStateView(
            icon = Icons.Outlined.Inventory2,
            title = "No hay productos",
            subtitle = "Agregá tu primer producto para comenzar",
            actionLabel = "Agregar producto",
            onAction = {},
        )
    }
}
