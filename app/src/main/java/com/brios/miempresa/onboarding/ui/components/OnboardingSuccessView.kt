package com.brios.miempresa.onboarding.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.CompanyAvatar
import com.brios.miempresa.core.ui.theme.AppDimensions
import kotlinx.coroutines.delay

private val decorationStarSize = 30
private val decorationConfettiSize = 24
private val decorationDiamondSize = 20

@Composable
fun OnboardingSuccessView(
    companyName: String,
    whatsappCountryCode: String,
    whatsappNumber: String,
    logoUri: String?,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
         val mediaPlayer = MediaPlayer.create(context, R.raw.onboarding_success)
         mediaPlayer?.start()
         mediaPlayer?.setOnCompletionListener { it.release() }

        delay(5000)
        onNavigateToHome()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = AppDimensions.largePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.8f))

        // Celebration area with decorations
        Box(
            contentAlignment = Alignment.Center,
        ) {
            // Animated decoration elements
            FloatingDecoration(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-20).dp, y = (-16).dp),
                content = {
                    Text(
                        text = "★",
                        fontSize = decorationStarSize.sp,
                        color = Color(0xFFFFB800),
                    )
                },
                durationMillis = 3000
            )

            FloatingDecoration(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-12).dp),
                content = {
                    Text(
                        text = "🎉",
                        fontSize = decorationConfettiSize.sp,
                    )
                },
                durationMillis = 4000,
                initialDelayMillis = 500
            )

            FloatingDecoration(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-24).dp, y = 8.dp),
                content = {
                    Text(
                        text = "💎",
                        fontSize = decorationDiamondSize.sp,
                    )
                },
                durationMillis = 3500,
                initialDelayMillis = 1000
            )

            // Main orange circle with white checkmark
            AnimatedCheckCircle()
        }

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))

        // Title
        Text(
            text = stringResource(R.string.onboarding_success_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

        // Subtitle
        Text(
            text = stringResource(R.string.onboarding_success_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

        // Company summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimensions.OnboardingSuccess.summaryCardCornerRadius),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(AppDimensions.OnboardingSuccess.summaryCardPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Company avatar (initials or logo)
                CompanyAvatar(
                    companyName = companyName,
                    logoUrl = logoUri,
                    size = AppDimensions.OnboardingSuccess.avatarSize,
                )

                Spacer(modifier = Modifier.width(AppDimensions.mediumPadding))

                // Company info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = companyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Sms,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "$whatsappCountryCode $whatsappNumber",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Verified badge
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimensions.defaultIconSize),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // CTA button (sticky bottom)
        Button(
            onClick = onNavigateToHome,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.OnboardingSuccess.ctaButtonHeight),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            shape = RoundedCornerShape(AppDimensions.OnboardingSuccess.ctaCornerRadius),
        ) {
            Text(
                text = stringResource(R.string.onboarding_success_cta),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
    }
}

@Composable
private fun AnimatedCheckCircle() {
    val scale = remember { Animatable(0.5f) }
    val opacity = remember { Animatable(0f) }
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        opacity.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    Box(
        modifier =
            Modifier
                .size(AppDimensions.OnboardingSuccess.checkCircleSize)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    alpha = opacity.value
                    shadowElevation = 20.dp.toPx()
                    shape = CircleShape
                    spotShadowColor = primaryColor
                    ambientShadowColor = primaryColor
                    clip = false
                }
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(AppDimensions.OnboardingSuccess.checkIconSize),
        )
    }
}

@Composable
private fun FloatingDecoration(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    durationMillis: Int,
    initialDelayMillis: Int = 0,
) {
    val transition = rememberInfiniteTransition(label = "floating-decoration")

    val yOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = durationMillis, easing = androidx.compose.animation.core.EaseInOut),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(initialDelayMillis),
            ),
        label = "decoration-float",
    )

    Box(
        modifier = modifier.offset(y = yOffset.dp)
    ) {
        content()
    }
}

// ============================================================
// Previews
// ============================================================

@androidx.compose.ui.tooling.preview.Preview(
    name = "Success with Logo",
    showBackground = true,
)
@Composable
private fun PreviewOnboardingSuccessWithLogo() {
    com.brios.miempresa.core.ui.theme.MiEmpresaTheme {
        OnboardingSuccessView(
            companyName = "Mi Tienda",
            whatsappCountryCode = "+54",
            whatsappNumber = "11 2345-6789",
            logoUri = "content://some.local.uri",
            onNavigateToHome = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Success without Logo",
    showBackground = true,
)
@Composable
private fun PreviewOnboardingSuccessNoLogo() {
    com.brios.miempresa.core.ui.theme.MiEmpresaTheme {
        OnboardingSuccessView(
            companyName = "Super Empresa con Nombre Muy Largo SRL",
            whatsappCountryCode = "+54",
            whatsappNumber = "9 2233-4455",
            logoUri = null,
            onNavigateToHome = {},
        )
    }
}
