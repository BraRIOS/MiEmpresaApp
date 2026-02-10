package com.brios.miempresa.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

private const val CROSSFADE_DURATION_MILLIS = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriangleArrowRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    val fraction = state.distanceFraction.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                translationY = fraction * 80f
                scaleX = fraction.coerceIn(0.5f, 1f)
                scaleY = fraction.coerceIn(0.5f, 1f)
                alpha = if (isRefreshing) 1f else fraction
                clip = false
                compositingStrategy = CompositingStrategy.ModulateAlpha
            }
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    val frameworkPaint = paint.asFrameworkPaint()
                    frameworkPaint.color = Color.Black.copy(alpha = 0.2f).toArgb()
                    frameworkPaint.setShadowLayer(
                        6.dp.toPx(),
                        0f,
                        0f,
                        Color.Black.copy(alpha = 0.2f).toArgb()
                    )
                    canvas.drawCircle(center, size.minDimension / 2, paint)
                }
            }
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = isRefreshing,
            animationSpec = tween(durationMillis = CROSSFADE_DURATION_MILLIS),
            label = "refreshIndicator",
        ) { refreshing ->
            Box(
                contentAlignment = Alignment.Center,
            ) {
                if (refreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = color,
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Pull to refresh",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                rotationZ = fraction * 180f
                            },
                        tint = color,
                    )
                }
            }
        }
    }
}
