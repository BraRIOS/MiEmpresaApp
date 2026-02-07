package com.brios.miempresa.auth.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun SignInScreen(
    state: SignInState,
    onSignInClick: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeContent)
                .padding(horizontal = AppDimensions.largePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.3f))

        Surface(
            shape = RoundedCornerShape(AppDimensions.SignInScreen.logoCornerRadius),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            tonalElevation = 2.dp,
        ) {
            Image(
                modifier =
                    Modifier
                        .size(AppDimensions.SignInScreen.logoSize)
                        .padding(AppDimensions.mediumPadding),
                painter = painterResource(id = R.drawable.miempresa_logo_round),
                contentDescription = stringResource(id = R.string.app_logo),
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))

        Text(
            text = stringResource(id = R.string.sign_in_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

        Text(
            text = stringResource(id = R.string.sign_in_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

        OutlinedButton(
            onClick = onSignInClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.SignInScreen.googleButtonHeight),
            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = stringResource(id = R.string.google_icon),
                modifier = Modifier.size(AppDimensions.defaultIconSize),
                tint = androidx.compose.ui.graphics.Color.Unspecified,
            )
            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
            Text(
                text = stringResource(id = R.string.sign_in_with_google),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                ),
        ) {
            Row(
                modifier = Modifier.padding(AppDimensions.mediumPadding),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(AppDimensions.mediumIconSize)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = CircleShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AppDimensions.smallIconSize),
                    )
                }
                Spacer(modifier = Modifier.width(AppDimensions.mediumSmallPadding))
                Text(
                    text =
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                ),
                            ) {
                                append(stringResource(id = R.string.privacy_first))
                            }
                            withStyle(
                                SpanStyle(color = MaterialTheme.colorScheme.onSurface),
                            ) {
                                append(stringResource(id = R.string.privacy_body))
                            }
                            withStyle(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textDecoration = TextDecoration.Underline,
                                ),
                            ) {
                                append(stringResource(id = R.string.privacy_emphasis))
                            }
                        },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text =
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    ) {
                        append(stringResource(id = R.string.sign_in_terms_prefix))
                    }
                    withStyle(
                        SpanStyle(color = MaterialTheme.colorScheme.primary),
                    ) {
                        append(stringResource(id = R.string.sign_in_terms))
                    }
                    withStyle(
                        SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    ) {
                        append(stringResource(id = R.string.sign_in_and))
                    }
                    withStyle(
                        SpanStyle(color = MaterialTheme.colorScheme.primary),
                    ) {
                        append(stringResource(id = R.string.sign_in_privacy_policy))
                    }
                    withStyle(
                        SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    ) {
                        append(".")
                    }
                },
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
    }
}

@Preview
@Composable
fun SignInScreenPreview() {
    SignInScreen(
        state = SignInState(),
        onSignInClick = {},
    )
}
