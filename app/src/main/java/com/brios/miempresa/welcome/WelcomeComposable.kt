package com.brios.miempresa.welcome

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.brios.miempresa.R
import com.google.android.gms.common.SignInButton


@Composable
fun WelcomeComposable(
    state: SignInState,
    onSignInClick: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.miempresa_ic_launcher_background))
            .windowInsetsPadding(WindowInsets.safeContent)
            .padding(top = 80.dp),
        verticalArrangement = Arrangement.spacedBy(76.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(id = R.drawable.miempresa_logo_round),
            contentDescription = "App logo"
        )

        GoogleSignInButton(onClick = onSignInClick)

    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    AndroidView(
        modifier = Modifier
            .wrapContentHeight(),
        factory = { context ->
            SignInButton(context).apply {
                setSize(SignInButton.SIZE_WIDE)
                setOnClickListener { onClick() }
                setColorScheme(SignInButton.COLOR_DARK)
            }
        }
    )
}

@Preview
@Composable
fun WelcomeComposablePreview() {
    WelcomeComposable(
        state = SignInState(),
        onSignInClick = {}
    )
}