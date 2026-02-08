package com.brios.miempresa.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun WelcomeScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToMyStores: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = AppDimensions.largePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.miempresa_logo_round),
            contentDescription = stringResource(R.string.app_logo),
            modifier =
                Modifier
                    .size(AppDimensions.WelcomeScreen.logoSize)
                    .clip(CircleShape),
        )

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))

        Text(
            text = stringResource(R.string.welcome_tagline),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Admin card — primary bg
        WelcomeActionCard(
            icon = Icons.Outlined.Storefront,
            title = stringResource(R.string.welcome_admin_button),
            subtitle = stringResource(R.string.welcome_admin_subtitle),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            iconCircleColor = Color.White.copy(alpha = 0.2f),
            onClick = onNavigateToSignIn,
            showGrid = true,
        )

        Spacer(modifier = Modifier.height(AppDimensions.mediumLargePadding))

        // Explore card — white bg, outlined border
        WelcomeActionCard(
            icon = Icons.Outlined.Search,
            title = stringResource(R.string.welcome_client_button),
            subtitle = stringResource(R.string.welcome_client_subtitle),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            iconCircleColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            iconTint = MaterialTheme.colorScheme.primary,
            arrowTint = MaterialTheme.colorScheme.outlineVariant,
            subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
            hasBorder = true,
            onClick = onNavigateToMyStores,
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text =
                buildAnnotatedString {
                    append(stringResource(R.string.welcome_footer))
                    append(" • ")
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(stringResource(R.string.welcome_terms))
                    }
                },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
    }
}

@Composable
private fun WelcomeActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    iconCircleColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = contentColor,
    arrowTint: Color = contentColor.copy(alpha = 0.7f),
    subtitleColor: Color = contentColor.copy(alpha = 0.8f),
    hasBorder: Boolean = false,
    showGrid: Boolean = false,
) {
    val cardModifier =
        if (hasBorder) {
            modifier
                .fillMaxWidth()
                .border(
                    width = AppDimensions.smallBorderWidth,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(AppDimensions.WelcomeScreen.actionCardCornerRadius),
                )
        } else {
            modifier.fillMaxWidth()
        }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(AppDimensions.WelcomeScreen.actionCardCornerRadius),
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        elevation =
            if (hasBorder) {
                CardDefaults.cardElevation(defaultElevation = 0.dp)
            } else {
                CardDefaults.cardElevation()
            },
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (showGrid) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val step = 20.dp.toPx()
                    val radius = 1.dp.toPx()
                    val dotColor = contentColor.copy(alpha = 0.15f)

                    for (x in 5..size.width.toInt() step step.toInt()) {
                        for (y in 20..size.height.toInt() step step.toInt()) {
                            drawCircle(
                                color = dotColor,
                                radius = radius,
                                center = Offset(x.toFloat(), y.toFloat()),
                            )
                        }
                    }
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick)
                        .padding(AppDimensions.WelcomeScreen.actionCardPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
            ) {
                Column {
                    Box(
                        modifier =
                            Modifier
                                .size(AppDimensions.WelcomeScreen.actionCardIconContainerSize)
                                .clip(RoundedCornerShape(AppDimensions.WelcomeScreen.actionCardIconCornerRadius))
                                .background(iconCircleColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(AppDimensions.defaultIconSize),
                        )
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    )
                    Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = subtitleColor,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = arrowTint,
                    modifier = Modifier.size(AppDimensions.defaultIconSize),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    MiEmpresaTheme {
        WelcomeScreen(
            onNavigateToSignIn = {},
            onNavigateToMyStores = {},
        )
    }
}
