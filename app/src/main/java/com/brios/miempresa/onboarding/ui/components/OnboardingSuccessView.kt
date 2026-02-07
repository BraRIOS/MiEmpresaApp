package com.brios.miempresa.onboarding.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.brios.miempresa.R
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
    LaunchedEffect(Unit) {
        delay(5000)
        onNavigateToHome()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = AppDimensions.largePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.8f))

        // Celebration area with decorations
        Box(
            contentAlignment = Alignment.Center,
        ) {
            // Star decoration (top-left)
            Text(
                text = "★",
                fontSize = decorationStarSize.sp,
                color = Color(0xFFFFB800),
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-20).dp, y = (-16).dp),
            )

            // Confetti decoration (top-right)
            Text(
                text = "🎉",
                fontSize = decorationConfettiSize.sp,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-12).dp),
            )

            // Diamond decoration (bottom-left)
            Text(
                text = "💎",
                fontSize = decorationDiamondSize.sp,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-24).dp, y = 8.dp),
            )

            // Main orange circle with white checkmark
            Box(
                modifier =
                    Modifier
                        .size(AppDimensions.OnboardingSuccess.checkCircleSize)
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
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(AppDimensions.OnboardingSuccess.summaryCardPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Company avatar (initials or logo)
                Box(
                    modifier =
                        Modifier
                            .size(AppDimensions.OnboardingSuccess.avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (logoUri != null) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = stringResource(R.string.company_logo, companyName),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        val initials =
                            companyName
                                .split(" ")
                                .take(2)
                                .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

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
