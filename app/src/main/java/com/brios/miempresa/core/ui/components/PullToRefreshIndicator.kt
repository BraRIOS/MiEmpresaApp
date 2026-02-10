package com.brios.miempresa.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
            .graphicsLayer {
                translationY = fraction * 80f
                scaleX = fraction.coerceIn(0.5f, 1f)
                scaleY = fraction.coerceIn(0.5f, 1f)
                alpha = if (isRefreshing) 1f else fraction
                clip = false
            }
            .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
            .size(40.dp)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = isRefreshing,
            animationSpec = tween(durationMillis = CROSSFADE_DURATION_MILLIS),
            label = "refreshIndicator",
        ) { refreshing ->
            Box(
                modifier = Modifier.size(40.dp),
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
