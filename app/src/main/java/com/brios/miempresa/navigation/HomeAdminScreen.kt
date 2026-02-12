package com.brios.miempresa.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.brios.miempresa.R
import com.brios.miempresa.categories.ui.CategoriesContent
import com.brios.miempresa.categories.ui.CategoriesUiState
import com.brios.miempresa.categories.ui.CategoriesViewModel
import com.brios.miempresa.config.ui.ConfigFormState
import com.brios.miempresa.config.ui.ConfigScreen
import com.brios.miempresa.config.ui.ConfigUiState
import com.brios.miempresa.config.ui.ConfigViewModel
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.products.ui.ProductsContent
import com.brios.miempresa.products.ui.ProductsUiState
import com.brios.miempresa.products.ui.ProductsViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeAdminScreen(
    navController: NavHostController,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    onNavigateToOrders: () -> Unit = {},
    onNavigateToWelcome: () -> Unit = {},
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
            onNavigateToOrders = onNavigateToOrders,
            onNavigateToWelcome = onNavigateToWelcome,
        )
    },
) {
    val productsUiState by productsViewModel.uiState.collectAsStateWithLifecycle()
    val categoriesUiState by categoriesViewModel.uiState.collectAsStateWithLifecycle()
    val configUiState by configViewModel.uiState.collectAsStateWithLifecycle()
    val configForm by configViewModel.form.collectAsStateWithLifecycle()

    HomeAdminScreenContent(
        navController = navController,
        onNavigateToAddProduct = onNavigateToAddProduct,
        onNavigateToProductDetail = onNavigateToProductDetail,
        onNavigateToAddCategory = onNavigateToAddCategory,
        onNavigateToCategoryDetail = onNavigateToCategoryDetail,
        productsUiState = productsUiState,
        categoriesUiState = categoriesUiState,
        configUiState = configUiState,
        configForm = configForm,
        onSaveConfig = configViewModel::save,
        productsContent = productsContent,
        categoriesContent = categoriesContent,
        configContent = configContent,
    )
}

@Composable
fun HomeAdminScreenContent(
    navController: NavHostController,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    productsUiState: ProductsUiState,
    categoriesUiState: CategoriesUiState,
    configUiState: ConfigUiState = ConfigUiState.Loading,
    configForm: ConfigFormState = ConfigFormState(),
    onSaveConfig: () -> Unit = {},
    productsContent: @Composable (Modifier) -> Unit,
    categoriesContent: @Composable (Modifier) -> Unit,
    configContent: @Composable (Modifier) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val showFab = when (selectedTab) {
        0 -> productsUiState !is ProductsUiState.Empty
        1 -> categoriesUiState !is CategoriesUiState.Empty
        else -> false
    }

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
                    actions = {
                        if (selectedTab == 2 && configForm.isFormValid) {
                            val isSaving = configUiState is ConfigUiState.Saving
                            Text(
                                text = stringResource(R.string.save),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = if (isSaving) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier
                                    .clickable(enabled = !isSaving) { onSaveConfig() }
                                    .padding(horizontal = AppDimensions.mediumSmallPadding),
                            )
                        }
                    },
                )
            },
            bottomBar = {
                BottomBar(
                    selectedTabIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            },
            floatingActionButton = {
                if (showFab) {
                    when (selectedTab) {
                        0 -> {
                            FABPaddingBottom(
                                onClick = onNavigateToAddProduct,
                                contentDescription = stringResource(R.string.add_product)
                            )
                        }

                        1 -> {
                            FABPaddingBottom(
                                onClick = onNavigateToAddCategory,
                                contentDescription = stringResource(R.string.add_category)
                            )
                        }
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
        modifier = Modifier
            .padding(bottom = AppDimensions.mediumPadding)
        ) {
        FloatingActionButton(
            modifier = Modifier.size(AppDimensions.mainFABSize),
            onClick = onClick,
//            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                icon,
                contentDescription,
                Modifier.size(AppDimensions.mainFABIconSize)
            )
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
            onNavigateToProductDetail = {},
            onNavigateToAddCategory = {},
            onNavigateToCategoryDetail = {},
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
            onNavigateToProductDetail = {},
            onNavigateToAddCategory = {},
            onNavigateToCategoryDetail = {},
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
