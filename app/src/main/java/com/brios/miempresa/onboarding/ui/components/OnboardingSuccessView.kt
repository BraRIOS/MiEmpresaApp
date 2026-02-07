package com.brios.miempresa.onboarding.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun OnboardingSuccessView(
    companyName: String,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onNavigateToHome()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(AppDimensions.extraLargePadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(AppDimensions.emptyStateIconSize),
            tint = SuccessGreen,
        )

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))

        Text(
            text = stringResource(R.string.onboarding_success_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

        Text(
            text = stringResource(R.string.onboarding_success_subtitle, companyName),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

        FilledTonalButton(
            onClick = onNavigateToHome,
        ) {
            Text(
                text = stringResource(R.string.onboarding_success_cta),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
