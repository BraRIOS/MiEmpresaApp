package com.brios.miempresa.auth.ui

import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.google.android.gms.common.SignInButton

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
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeContent),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            modifier = Modifier.size(AppDimensions.SignInScreen.logoSize),
            painter = painterResource(id = R.drawable.miempresa_logo_round),
            contentDescription = stringResource(id = R.string.app_logo),
        )

        Spacer(
            modifier = Modifier.height(AppDimensions.SignInScreen.spaceBetweenLogoAndSignInButton),
        )

        GoogleSignInButton(onClick = onSignInClick)

        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

        Text(
            text = stringResource(id = R.string.privacy_info),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppDimensions.largePadding),
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    AndroidView(
        modifier =
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(AppDimensions.largePadding),
        factory = { context ->
            SignInButton(context).apply {
                setSize(SignInButton.SIZE_WIDE)
                setOnClickListener { onClick() }
                setColorScheme(SignInButton.COLOR_DARK)
            }
        },
    )
}

@Preview
@Composable
fun WelcomeComposablePreview() {
    SignInScreen(
        state = SignInState(),
        onSignInClick = {},
    )
}
