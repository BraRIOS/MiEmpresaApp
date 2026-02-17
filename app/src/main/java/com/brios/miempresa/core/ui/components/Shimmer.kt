package com.brios.miempresa.core.ui.components

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.brios.miempresa.core.ui.theme.AppDimensions

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate",
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )

    background(brush)
}

@Composable
fun ProductItemShimmer() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column {
            Row(
                modifier =
                Modifier
                    .padding(AppDimensions.mediumPadding)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Image placeholder
                Box(
                    modifier = Modifier
                        .size(AppDimensions.productItemImageSize)
                        .clip(RoundedCornerShape(AppDimensions.smallCornerRadius))
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.width(AppDimensions.mediumPadding))

                Column(
                    modifier = Modifier.height(AppDimensions.productItemImageSize),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
                    ) {
                        // Title placeholder
                        Box(
                            modifier = Modifier
                                .height(16.dp)
                                .width(150.dp)
                                .clip(RoundedCornerShape(AppDimensions.extraSmallPadding))
                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Subtitle/Price placeholder
                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .width(80.dp)
                                .clip(RoundedCornerShape(AppDimensions.extraSmallPadding))
                                .shimmerEffect()
                        )
                    }

                    // Bottom row placeholders (badge, actions)
                     Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding, Alignment.End),
                         modifier = Modifier.fillMaxWidth()
                    ) {
                         // Badge placeholder
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(60.dp)
                                .clip(RoundedCornerShape(AppDimensions.smallCornerRadius))
                                .shimmerEffect()
                        )

                         Spacer(modifier = Modifier.weight(1f))

                         // Icon placeholder
                         Box(
                             modifier = Modifier
                                 .size(24.dp)
                                 .clip(RoundedCornerShape(AppDimensions.extraSmallPadding))
                                 .shimmerEffect()
                         )
                    }

                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}
