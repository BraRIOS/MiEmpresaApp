package com.brios.miempresa.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.brios.miempresa.R
import com.brios.miempresa.categories.ui.CategoriesContent
import com.brios.miempresa.config.ui.ConfigScreen
import com.brios.miempresa.products.ui.ProductsContent
import kotlinx.coroutines.launch

@Composable
fun HomeAdminScreen(
    navController: NavHostController,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabTitles =
        listOf(
            stringResource(R.string.home_title),
            stringResource(R.string.categories_title),
            stringResource(R.string.config_title),
        )

    DrawerComposable(
        navController = navController,
        drawerState = drawerState,
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    navController = navController,
                    title = tabTitles[selectedTab],
                    openDrawer = { scope.launch { drawerState.open() } },
                )
            },
            bottomBar = {
                BottomBar(
                    selectedTabIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            },
            floatingActionButton = {
                when (selectedTab) {
                    0 -> {
                        FloatingActionButton(onClick = onNavigateToAddProduct) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_product))
                        }
                    }
                    1 -> {
                        FloatingActionButton(onClick = onNavigateToAddCategory) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_category))
                        }
                    }
                }
            },
        ) { paddingValues ->
            when (selectedTab) {
                0 ->
                    ProductsContent(
                        modifier = Modifier.padding(paddingValues),
                        onNavigateToAddProduct = onNavigateToAddProduct,
                        onNavigateToProductDetail = onNavigateToProductDetail,
                    )
                1 ->
                    CategoriesContent(
                        modifier = Modifier.padding(paddingValues),
                        onNavigateToAddCategory = onNavigateToAddCategory,
                        onNavigateToCategoryDetail = onNavigateToCategoryDetail,
                    )
                2 ->
                    ConfigScreen(modifier = Modifier.padding(paddingValues))
            }
        }
    }
}
