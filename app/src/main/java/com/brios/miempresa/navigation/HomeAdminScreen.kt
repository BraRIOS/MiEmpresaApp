package com.brios.miempresa.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.brios.miempresa.R
import com.brios.miempresa.categories.ui.CategoriesContent
import com.brios.miempresa.config.ui.ConfigScreen
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.products.ui.ProductsContent
import kotlinx.coroutines.launch

@Composable
fun HomeAdminScreen(
    navController: NavHostController,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    productsContent: @Composable (Modifier) -> Unit = { modifier ->
        ProductsContent(
            modifier = modifier,
            onNavigateToAddProduct = onNavigateToAddProduct,
            onNavigateToProductDetail = onNavigateToProductDetail,
        )
    },
    categoriesContent: @Composable (Modifier) -> Unit = { modifier ->
        CategoriesContent(
            modifier = modifier,
            onNavigateToAddCategory = onNavigateToAddCategory,
            onNavigateToCategoryDetail = onNavigateToCategoryDetail,
        )
    },
    configContent: @Composable (Modifier) -> Unit = { modifier ->
        ConfigScreen(modifier = modifier)
    },
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
                        FABPaddingBottom(onNavigateToAddProduct,contentDescription = stringResource(R.string.add_product))
                    }

                    1 -> {
                        FABPaddingBottom(onNavigateToAddCategory, contentDescription = stringResource(R.string.add_category))
                    }
                }
            },
        ) { paddingValues ->
            when (selectedTab) {
                0 -> productsContent(Modifier.padding(paddingValues))
                1 -> categoriesContent(Modifier.padding(paddingValues))
                2 -> configContent(Modifier.padding(paddingValues))
            }
        }
    }
}

@Composable
private fun FABPaddingBottom(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    contentDescription: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(bottom = AppDimensions.mediumPadding),
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(icon, contentDescription)
        }
    }
}

@Preview
@Composable
private fun HomeAdminScreenPreview() {
    MiEmpresaTheme {
        HomeAdminScreen(
            navController = rememberNavController(),
            onNavigateToAddProduct = {},
            onNavigateToProductDetail = {},
            onNavigateToAddCategory = {},
            onNavigateToCategoryDetail = {},
            productsContent = { modifier ->
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Products Content")
                }
            },
            categoriesContent = { modifier ->
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Categories Content")
                }
            },
            configContent = { modifier ->
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Config Content")
                }
            },
        )
    }
}
