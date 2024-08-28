package com.brios.miempresa.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.brios.miempresa.categories.Categories
import com.brios.miempresa.home.HomeComposable
import com.brios.miempresa.model.MainViewModel
import com.brios.miempresa.product.ProductDetails

@Composable
fun NavHostComposable(innerPadding: PaddingValues, navController: NavHostController, viewModel: MainViewModel) {
    NavHost(
        navController = navController,
        startDestination = MiEmpresaScreen.Home.name,
        modifier = Modifier.fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 4.dp)
    ) {
        composable(route = MiEmpresaScreen.Home.name) {
            HomeComposable(
                viewModel,
                onNavigateToProductDetail = { navController.navigate(MiEmpresaScreen.Product.name) }
            )
        }
        composable(route = MiEmpresaScreen.Product.name) {
            ProductDetails( viewModel )
        }
        composable(route = MiEmpresaScreen.Categories.name) {
            Categories( viewModel )
        }
    }
}
