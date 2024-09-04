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
import com.brios.miempresa.categories.CategoriesComposable
import com.brios.miempresa.product.ProductDetails
import com.brios.miempresa.product.ProductsComposable

@Composable
fun NavHostComposable(innerPadding: PaddingValues, navController: NavHostController, viewModel: TopBarViewModel) {
    NavHost(
        navController = navController,
        startDestination = MiEmpresaScreen.Products.name,
        modifier = Modifier.fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 4.dp)
    ) {
        composable(route = MiEmpresaScreen.Products.name) {
            ProductsComposable(
                viewModel,
                onNavigateToProductDetail = { navController.navigate(MiEmpresaScreen.Product.name) }
            )
        }
        composable(route = MiEmpresaScreen.Product.name) {
            ProductDetails( viewModel )
        }
        composable(route = MiEmpresaScreen.Categories.name) {
            CategoriesComposable( viewModel )
        }
    }
}
