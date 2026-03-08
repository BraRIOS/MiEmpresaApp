package com.brios.miempresa.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.brios.miempresa.R
import com.brios.miempresa.auth.ui.SignInViewModel
import com.brios.miempresa.categories.ui.CategoriesContent
import com.brios.miempresa.categories.ui.CategoriesUiState
import com.brios.miempresa.categories.ui.CategoriesViewModel
import com.brios.miempresa.config.ui.ConfigScreen
import com.brios.miempresa.config.ui.ConfigViewModel
import com.brios.miempresa.core.ui.components.MiEmpresaFAB
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.products.ui.ProductsContent
import com.brios.miempresa.products.ui.ProductsUiState
import com.brios.miempresa.products.ui.ProductsViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun HomeAdminScreen(
    navController: NavHostController,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    onNavigateToOrders: () -> Unit = {},
    onNavigateToEditCompany: () -> Unit = {},
    onNavigateToWelcome: () -> Unit = {},
    onNavigateToClientCatalog: (String) -> Unit = {},
    signInViewModel: SignInViewModel = hiltViewModel(),
    productsViewModel: ProductsViewModel = hiltViewModel(),
    categoriesViewModel: CategoriesViewModel = hiltViewModel(),
    configViewModel: ConfigViewModel = hiltViewModel(),
    productsContent: @Composable (Modifier) -> Unit = { modifier ->
        ProductsContent(
            modifier = modifier,
            onNavigateToAddProduct = onNavigateToAddProduct,
            onNavigateToProductDetail = onNavigateToProductDetail,
            viewModel = productsViewModel,
        )
    },
    categoriesContent: @Composable (Modifier) -> Unit = { modifier ->
        CategoriesContent(
            modifier = modifier,
            onNavigateToAddCategory = onNavigateToAddCategory,
            onNavigateToCategoryDetail = onNavigateToCategoryDetail,
            viewModel = categoriesViewModel,
        )
    },
    configContent: @Composable (Modifier) -> Unit = { modifier ->
        ConfigScreen(
            modifier = modifier,
            viewModel = configViewModel,
            onNavigateToEditCompany = onNavigateToEditCompany,
            onNavigateToOrders = onNavigateToOrders,
            onNavigateToWelcome = onNavigateToWelcome,
            onNavigateToClientCatalog = onNavigateToClientCatalog,
        )
    },
) {
    val productsUiState by productsViewModel.uiState.collectAsStateWithLifecycle()
    val categoriesUiState by categoriesViewModel.uiState.collectAsStateWithLifecycle()
    val homeSavedStateHandle = navController.getBackStackEntry(MiEmpresaRoutes.home).savedStateHandle

    LaunchedEffect(homeSavedStateHandle) {
        homeSavedStateHandle.getStateFlow("products_sync_feedback", false).collect { pending ->
            if (pending) {
                productsViewModel.syncAndNotifyAfterFormEdit()
                homeSavedStateHandle["products_sync_feedback"] = false
            }
        }
    }

    LaunchedEffect(homeSavedStateHandle) {
        homeSavedStateHandle.getStateFlow("categories_sync_feedback", false).collect { pending ->
            if (pending) {
                categoriesViewModel.syncAndNotifyAfterFormEdit()
                homeSavedStateHandle["categories_sync_feedback"] = false
            }
        }
    }

    HomeAdminScreenContent(
        navController = navController,
        onNavigateToAddProduct = onNavigateToAddProduct,
        onNavigateToAddCategory = onNavigateToAddCategory,
        productsUiState = productsUiState,
        categoriesUiState = categoriesUiState,
        signInViewModel = signInViewModel,
        productsContent = productsContent,
        categoriesContent = categoriesContent,
        configContent = configContent,
    )
}

@Composable
fun HomeAdminScreenContent(
    navController: NavHostController,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    productsUiState: ProductsUiState,
    categoriesUiState: CategoriesUiState,
    signInViewModel: SignInViewModel? = null,
    productsContent: @Composable (Modifier) -> Unit,
    categoriesContent: @Composable (Modifier) -> Unit,
    configContent: @Composable (Modifier) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(AdminTopLevelTab.Products) }

    val showFab = when (selectedTab) {
        AdminTopLevelTab.Products -> productsUiState !is ProductsUiState.Empty
        AdminTopLevelTab.Categories -> categoriesUiState !is CategoriesUiState.Empty
        else -> false
    }

    DrawerComposable(
        navController = navController,
        drawerState = drawerState,
        signInViewModel = signInViewModel,
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = stringResource(selectedTab.titleRes),
                    openDrawer = { scope.launch { drawerState.open() } },
                )
            },
            bottomBar = {
                BottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            },
            floatingActionButton = {
                if (showFab) {
                    when (selectedTab) {
                        AdminTopLevelTab.Products -> {
                            MiEmpresaFAB(
                                onClick = onNavigateToAddProduct,
                                contentDescription = stringResource(R.string.add_product),
                            )
                        }

                        AdminTopLevelTab.Categories -> {
                            MiEmpresaFAB(
                                onClick = onNavigateToAddCategory,
                                contentDescription = stringResource(R.string.add_category),
                            )
                        }

                        AdminTopLevelTab.Company -> Unit
                    }
                }
            },
        ) { paddingValues ->
            when (selectedTab) {
                AdminTopLevelTab.Products -> productsContent(Modifier.padding(paddingValues))
                AdminTopLevelTab.Categories -> categoriesContent(Modifier.padding(paddingValues))
                AdminTopLevelTab.Company -> configContent(Modifier.padding(paddingValues))
            }
        }
    }
}

@Preview
@Composable
private fun HomeAdminScreenPreview() {
    MiEmpresaTheme {
        HomeAdminScreenContent(
            navController = rememberNavController(),
            onNavigateToAddProduct = {},
            onNavigateToAddCategory = {},
            productsUiState = ProductsUiState.Empty,
            categoriesUiState = CategoriesUiState.Empty,
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

@Preview
@Composable
private fun HomeAdminScreenWithFABPreview() {
    MiEmpresaTheme {
        HomeAdminScreenContent(
            navController = rememberNavController(),
            onNavigateToAddProduct = {},
            onNavigateToAddCategory = {},
            productsUiState = ProductsUiState.Loading,
            categoriesUiState = CategoriesUiState.Empty,
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
