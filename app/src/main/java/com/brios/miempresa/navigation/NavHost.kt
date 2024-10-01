package com.brios.miempresa.navigation

import android.app.Activity
import android.content.Context
import android.widget.Toast
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
import com.brios.miempresa.categories.CategoriesComposable
import com.brios.miempresa.initializer.InitializerScreen
import com.brios.miempresa.product.ProductDetails
import com.brios.miempresa.product.ProductsComposable
import com.brios.miempresa.signin.AuthState
import com.brios.miempresa.signin.SignInScreen
import com.brios.miempresa.signin.SignInViewModel

@Composable
fun NavHostComposable(applicationContext: Context, navController: NavHostController) {
    val signInViewModel = hiltViewModel<SignInViewModel>()
    NavHost(
        navController = navController,
        startDestination =
        if(signInViewModel.getSignedInUser() != null)
            MiEmpresaScreen.Initializer.name
        else
            MiEmpresaScreen.SignIn.name,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = MiEmpresaScreen.SignIn.name) {
            val signInState by signInViewModel.signInStateFlow.collectAsStateWithLifecycle()
            val authState by signInViewModel.authStateFlow.collectAsStateWithLifecycle()
            val activity = LocalContext.current as Activity

            val signInSuccess = stringResource(R.string.sign_in_success)
            val authorizationSuccess = stringResource(R.string.authorization_success)
            val authorizationFailed = stringResource(R.string.authorization_failed)

            LaunchedEffect(key1 = signInState.isSignInSuccessful) {
                if(signInState.isSignInSuccessful) {
                    Toast.makeText(applicationContext, signInSuccess, Toast.LENGTH_LONG).show()

                    signInViewModel.authorizeDriveAndSheets(activity)
                    signInViewModel.resetSignInState()
                }
            }

            val authorizationLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult()
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
                    authorizationLauncher.launch(
                        IntentSenderRequest.Builder((authState as AuthState.PendingAuth).intentSender).build()
                    )
                }
                else if (authState is AuthState.Authorized && signInViewModel.getSignedInUser()!=null) {
                    navController.navigate(MiEmpresaScreen.Initializer.name){
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            }

            SignInScreen(
                signInState,
                onSignInClick = {
                    signInViewModel.signIn(activity)
                }
            )
        }
        composable(route = MiEmpresaScreen.Initializer.name) {
            InitializerScreen(
                navController = navController
            )
        }
        composable(route = MiEmpresaScreen.Products.name) {
            ProductsComposable(
                navController = navController
            )
        }
        composable(
            route = MiEmpresaScreen.Product.name + "/{rowIndex}",
            arguments = listOf(navArgument("rowIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val rowIndex = backStackEntry.arguments?.getInt("rowIndex")
            ProductDetails(
                navController = navController,
                rowIndex = rowIndex
            )
        }
        composable(route = MiEmpresaScreen.Categories.name) {
            CategoriesComposable(
                navController = navController
            )
        }
    }
}
