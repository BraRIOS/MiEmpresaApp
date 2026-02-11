package com.brios.miempresa.navigation

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.brios.miempresa.R
import com.brios.miempresa.auth.domain.AuthState
import com.brios.miempresa.auth.ui.PostAuthDestination
import com.brios.miempresa.auth.ui.SignInScreen
import com.brios.miempresa.auth.ui.SignInViewModel
import com.brios.miempresa.auth.ui.WelcomeScreen
import com.brios.miempresa.categories.ui.CategoryFormScreen
import com.brios.miempresa.onboarding.ui.OnboardingScreen
import com.brios.miempresa.products.ui.ProductFormScreen

@Composable
fun NavHostComposable(
    applicationContext: Context,
    navController: NavHostController,
) {
    val signInViewModel = hiltViewModel<SignInViewModel>()
    val isAlreadySignedIn = signInViewModel.getSignedInUser() != null

    // If already signed in, skip Welcome and check Drive authorization
    if (isAlreadySignedIn) {
        LaunchedEffect(Unit) {
            val authState = signInViewModel.checkDriveAuthorization()
            when (authState) {
                is AuthState.Authorized -> {
                    signInViewModel.determinePostAuthDestination()
                }
                is AuthState.PendingAuth -> {
                    signInViewModel.updateAuthState(authState)
                    navController.navigate(MiEmpresaScreen.SignIn.name) {
                        popUpTo(MiEmpresaScreen.Onboarding.name) { inclusive = true }
                    }
                }
                else -> {
                    navController.navigate(MiEmpresaScreen.SignIn.name) {
                        popUpTo(MiEmpresaScreen.Onboarding.name) { inclusive = true }
                    }
                }
            }
        }
    }

    val postAuthDest by signInViewModel.postAuthDestination.collectAsStateWithLifecycle()

    LaunchedEffect(postAuthDest) {
        when (postAuthDest) {
            is PostAuthDestination.Onboarding,
            is PostAuthDestination.CompanySelector,
            -> {
                navController.navigate(MiEmpresaScreen.Onboarding.name) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is PostAuthDestination.Home -> {
                navController.navigate(MiEmpresaScreen.Home.name) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            null -> {}
        }
    }

    // Start at Onboarding if already signed in (shows discovery loading),
    // otherwise start at Welcome for new/signed-out users
    val startDestination =
        if (isAlreadySignedIn) {
            MiEmpresaScreen.Onboarding.name
        } else {
            MiEmpresaScreen.Welcome.name
        }

    NavHost(
        navController = navController,
        startDestination = startDestination,
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
            val activity = LocalActivity.current as Activity

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

            SignInScreen(
                signInState,
                onSignInClick = {
                    signInViewModel.signIn(activity)
                },
            )
        }
        composable(route = MiEmpresaScreen.Onboarding.name) {
            val activity = LocalActivity.current as Activity
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(MiEmpresaScreen.Home.name) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSignOutRequested = {
                    signInViewModel.signOut(activity)
                    navController.navigate(MiEmpresaScreen.Welcome.name) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(route = MiEmpresaScreen.Home.name) {
            HomeAdminScreen(
                navController = navController,
                onNavigateToAddProduct = {
                    navController.navigate("${MiEmpresaScreen.Product.name}/add")
                },
                onNavigateToProductDetail = { productId ->
                    navController.navigate("${MiEmpresaScreen.Product.name}/$productId")
                },
                onNavigateToAddCategory = {
                    navController.navigate("${MiEmpresaScreen.Categories.name}/add")
                },
                onNavigateToCategoryDetail = { categoryId ->
                    navController.navigate("${MiEmpresaScreen.Categories.name}/$categoryId")
                },
            )
        }
        composable(route = "${MiEmpresaScreen.Product.name}/add") {
            ProductFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = {
                    navController.navigate("${MiEmpresaScreen.Categories.name}/add")
                },
            )
        }
        composable(
            route = "${MiEmpresaScreen.Product.name}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) {
            ProductFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = {
                    navController.navigate("${MiEmpresaScreen.Categories.name}/add")
                },
            )
        }
        composable(route = "${MiEmpresaScreen.Categories.name}/add") {
            CategoryFormScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "${MiEmpresaScreen.Categories.name}/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
        ) {
            CategoryFormScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
