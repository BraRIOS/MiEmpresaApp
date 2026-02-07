package com.brios.miempresa.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeContent)
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
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

        Text(
            text = stringResource(R.string.welcome_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNavigateToSignIn,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.welcome_admin_button))
        }

        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

        OutlinedButton(
            onClick = onNavigateToMyStores,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.welcome_client_button))
        }

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))
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
