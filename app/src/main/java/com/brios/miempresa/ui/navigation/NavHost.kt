package com.brios.miempresa.ui.navigation

import android.app.Activity
import android.content.Context
import android.widget.Toast
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.brios.miempresa.R
import com.brios.miempresa.ui.categories.CategoriesComposable
import com.brios.miempresa.ui.common.ScaffoldedScreenComposable
import com.brios.miempresa.ui.product.ProductDetails
import com.brios.miempresa.ui.product.ProductsComposable
import com.brios.miempresa.ui.sign_in.SignInViewModel
import com.brios.miempresa.ui.sign_in.WelcomeComposable

@Composable
fun NavHostComposable(applicationContext: Context, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = MiEmpresaScreen.Welcome.name,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = MiEmpresaScreen.Welcome.name) {
            val signInViewModel = hiltViewModel<SignInViewModel>()
            val state by signInViewModel.state.collectAsStateWithLifecycle()
            val activity = LocalContext.current as Activity
            LaunchedEffect(key1 = Unit) {
                if(signInViewModel.getSignedInUser() != null) {
                    navController.navigate(MiEmpresaScreen.Product.name)
                }
            }
            val signInSuccess = stringResource(R.string.sign_in_success)
            LaunchedEffect(key1 = state.isSignInSuccessful) {
                if(state.isSignInSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        signInSuccess,
                        Toast.LENGTH_LONG
                    ).show()

                    navController.navigate(MiEmpresaScreen.Product.name)
                    signInViewModel.resetState()
                }
            }
            WelcomeComposable(
                state,
                onSignInClick = {
                    signInViewModel.signIn(activity)
                }
            )
        }
        composable(route = MiEmpresaScreen.Products.name) {
            ScaffoldedScreenComposable(
                navController
            ){
                ProductsComposable(
                    onNavigateToProductDetail = {
                        navController.navigate(MiEmpresaScreen.Product.name)
                    }
                )
            }
        }
        composable(route = MiEmpresaScreen.Product.name) {
            ProductDetails( )
        }
        composable(route = MiEmpresaScreen.Categories.name) {
            CategoriesComposable( )
        }
    }
}
