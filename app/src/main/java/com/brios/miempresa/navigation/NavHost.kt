package com.brios.miempresa.navigation

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.brios.miempresa.R
import com.brios.miempresa.auth.domain.AuthState
import com.brios.miempresa.auth.ui.PostAuthDestination
import com.brios.miempresa.auth.ui.SignInScreen
import com.brios.miempresa.auth.ui.SignInViewModel
import com.brios.miempresa.auth.ui.WelcomeScreen
import com.brios.miempresa.onboarding.ui.OnboardingScreen

@Composable
fun NavHostComposable(
    applicationContext: Context,
    navController: NavHostController,
) {
    val signInViewModel = hiltViewModel<SignInViewModel>()
    NavHost(
        navController = navController,
        startDestination = MiEmpresaScreen.Welcome.name,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(route = MiEmpresaScreen.Welcome.name) {
            WelcomeScreen(
                onNavigateToSignIn = {
                    navController.navigate(MiEmpresaScreen.SignIn.name)
                },
                onNavigateToMyStores = {},
            )
        }
        composable(route = MiEmpresaScreen.SignIn.name) {
            val signInState by signInViewModel.signInStateFlow.collectAsStateWithLifecycle()
            val authState by signInViewModel.authStateFlow.collectAsStateWithLifecycle()
            val postAuthDest by signInViewModel.postAuthDestination.collectAsStateWithLifecycle()
            val activity = LocalContext.current as Activity

            val signInSuccess = stringResource(R.string.sign_in_success)
            val authorizationSuccess = stringResource(R.string.authorization_success)
            val authorizationFailed = stringResource(R.string.authorization_failed)

            LaunchedEffect(key1 = signInState.isSignInSuccessful) {
                if (signInState.isSignInSuccessful) {
                    Toast.makeText(applicationContext, signInSuccess, Toast.LENGTH_LONG).show()
                    signInViewModel.authorizeDriveAndSheets(activity)
                    signInViewModel.resetSignInState()
                }
            }

            val authorizationLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        Toast.makeText(applicationContext, authorizationSuccess, Toast.LENGTH_LONG).show()
                        signInViewModel.updateAuthState(AuthState.Authorized)
                    } else {
                        Toast.makeText(applicationContext, authorizationFailed, Toast.LENGTH_LONG).show()
                        signInViewModel.updateAuthState(AuthState.Unauthorized)
                        signInViewModel.signOut(activity)
                    }
                }

            LaunchedEffect(key1 = authState, key2 = signInViewModel.getSignedInUser()) {
                if (authState is AuthState.PendingAuth) {
                    (authState as AuthState.PendingAuth).intentSender?.let { intentSender ->
                        authorizationLauncher.launch(
                            IntentSenderRequest.Builder(intentSender).build(),
                        )
                    }
                } else if (authState is AuthState.Authorized && signInViewModel.getSignedInUser() != null) {
                    signInViewModel.determinePostAuthDestination()
                }
            }

            LaunchedEffect(key1 = postAuthDest) {
                when (postAuthDest) {
                    is PostAuthDestination.Onboarding -> {
                        navController.navigate(MiEmpresaScreen.Onboarding.name) {
                            popUpTo(MiEmpresaScreen.Welcome.name) { inclusive = false }
                        }
                    }
                    is PostAuthDestination.Home, is PostAuthDestination.CompanySelector -> {
                        navController.navigate(MiEmpresaScreen.Products.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    null -> {}
                }
            }

            SignInScreen(
                signInState,
                onSignInClick = {
                    signInViewModel.signIn(activity)
                },
            )
        }
        composable(route = MiEmpresaScreen.Onboarding.name) {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(MiEmpresaScreen.Products.name) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.navigate(MiEmpresaScreen.Welcome.name) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(route = MiEmpresaScreen.Products.name) {
            // TODO: Replace with ProductsScreen when implemented
            Text(text = "Products (placeholder)")
        }
    }
}
