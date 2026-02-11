package com.brios.miempresa.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.brios.miempresa.core.ui.theme.AppDimensions

@Composable
fun StateViewSpotIllustration(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerSize: Dp = AppDimensions.emptyStateIconSize,
    iconSize: Dp = AppDimensions.emptyStateInnerIconSize,
    iconTint: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    containerColor: List<Color> = listOf(MaterialTheme.colorScheme.surface, Color.White)
) {
    // Scale dots and offsets based on container size relative to the default 160dp
    val scaleFactor = containerSize / AppDimensions.emptyStateIconSize

    Box(
        modifier = modifier
            .size(containerSize)
            .background(
                brush = Brush.verticalGradient(colors = containerColor),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = iconTint,
        )

        PulsingDot(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16 * scaleFactor).dp, y = (24 * scaleFactor).dp),
            size = 12.dp * scaleFactor,
            color = iconTint.copy(alpha = 0.2f),
        )

        PulsingDot(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (24 * scaleFactor).dp, y = (-32 * scaleFactor).dp),
            size = 8.dp * scaleFactor,
            color = iconTint.copy(alpha = 0.3f),
            initialDelayMillis = 1000,
        )
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
