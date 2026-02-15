package com.brios.miempresa.catalog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.catalog.ui.components.CatalogProductCard
import com.brios.miempresa.catalog.ui.components.CompanyHeader
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.ui.components.CategoryFilterChip
import com.brios.miempresa.core.ui.components.CategorySelectorBottomSheet
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.NotFoundView
import com.brios.miempresa.core.ui.components.OfflineBanner
import com.brios.miempresa.core.ui.components.SearchBar
import com.brios.miempresa.core.ui.components.SearchBarVariant
import com.brios.miempresa.core.ui.components.TriangleArrowRefreshIndicator
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.products.data.ProductEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCart: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientCatalogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    ClientCatalogScreenContent(
        uiState = uiState,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refreshCatalog,
        onNavigateBack = onNavigateBack,
        onNavigateToCart = onNavigateToCart,
        onNavigateToHome = onNavigateToHome,
        onNavigateToProductDetail = onNavigateToProductDetail,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategoryToggle = viewModel::onCategoryToggle,
        onClearCategoryFilter = viewModel::clearCategoryFilter,
        onClearFilters = viewModel::clearFilters,
        onAddProductToCart = { viewModel.addProductToCart(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientCatalogScreenContent(
    uiState: ClientCatalogState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCart: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategoryToggle: (String) -> Unit,
    onClearCategoryFilter: () -> Unit,
    onClearFilters: () -> Unit,
    onAddProductToCart: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        state = pullToRefreshState,
        indicator = {
            TriangleArrowRefreshIndicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        when (uiState) {
            is ClientCatalogState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = stringResource(R.string.client_catalog_loading))
                }
            }

            is ClientCatalogState.Error -> {
                val message = uiState.message
                EmptyStateView(
                    icon = Icons.Outlined.SearchOff,
                    title = message,
                    subtitle = "",
                    actionLabel = stringResource(R.string.deeplink_retry),
                    onAction = onRefresh,
                )
            }

            is ClientCatalogState.Success,
            is ClientCatalogState.Empty,
            is ClientCatalogState.Offline,
            -> {
                val data = uiState.data
                val gridState = rememberLazyGridState()
                val collapseRangePx = with(LocalDensity.current) { 180.dp.toPx() }
                val collapseFraction by
                    remember(gridState, collapseRangePx, uiState) {
                        derivedStateOf {
                            if (uiState !is ClientCatalogState.Success) {
                                0f
                            } else if (gridState.firstVisibleItemIndex > 0) {
                                1f
                            } else {
                                (gridState.firstVisibleItemScrollOffset / collapseRangePx).coerceIn(0f, 1f)
                            }
                        }
                    }
                val showCollapsedTitle = uiState is ClientCatalogState.Success && collapseFraction > 0.45f
                var showCategorySelector by rememberSaveable(data.company.id) { mutableStateOf(false) }
                val categoryOptions =
                    remember(data.categories, data.company.id) {
                        data.categories.map { categoryName ->
                            Category(
                                id = categoryName,
                                name = categoryName,
                                iconEmoji = "",
                                companyId = data.company.id,
                            )
                        }
                    }

                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CatalogTopBar(
                        title = if (showCollapsedTitle) data.company.name else "",
                        cartCount = data.cartCount,
                        onNavigateBack = onNavigateBack,
                        onNavigateToCart = { onNavigateToCart(data.company.id) },
                    )

                    if (uiState is ClientCatalogState.Success) {
                        CollapsibleCompanyHeader(
                            company = data.company,
                            visibleFraction = 1f - collapseFraction,
                        )
                    } else {
                        CompanyHeader(
                            company = data.company,
                            modifier = Modifier.padding(top = AppDimensions.smallPadding),
                        )
                    }

                    CatalogFilterRow(
                        query = data.searchQuery,
                        selectedCategory = data.selectedCategory,
                        onQueryChange = onSearchQueryChange,
                        onCategoryClick = { showCategorySelector = true },
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    if (data.isOffline) {
                        OfflineBanner()
                    }

                    when (uiState) {
                        is ClientCatalogState.Success -> {
                            LazyVerticalGrid(
                                state = gridState,
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                                horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                                contentPadding =
                                    PaddingValues(
                                        start = AppDimensions.mediumPadding,
                                        end = AppDimensions.mediumPadding,
                                        top = AppDimensions.smallPadding,
                                        bottom =
                                            if (data.isAdminHybrid) {
                                                AppDimensions.extraLargePadding * 3
                                            } else {
                                                AppDimensions.largePadding
                                            },
                                    ),
                            ) {
                                items(
                                    items = uiState.data.products,
                                    key = { it.id },
                                ) { product ->
                                    CatalogProductCard(
                                        product = product,
                                        onClick = { onNavigateToProductDetail(product.id) },
                                        onAddToCart = { onAddProductToCart(product.id) },
                                    )
                                }
                            }
                        }

                        is ClientCatalogState.Empty -> {
                            if (uiState.hasActiveFilters) {
                                NotFoundView(
                                    modifier = Modifier.weight(1f),
                                    message = stringResource(R.string.client_catalog_empty_filtered),
                                    onAction = onClearFilters,
                                )
                            } else {
                                EmptyStateView(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Filled.ShoppingBag,
                                    title = stringResource(R.string.client_catalog_empty_title),
                                    subtitle = stringResource(R.string.client_catalog_empty_subtitle),
                                )
                            }
                        }

                        is ClientCatalogState.Offline -> {
                            EmptyStateView(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.CloudOff,
                                title = stringResource(R.string.client_catalog_offline_title),
                                subtitle = stringResource(R.string.client_catalog_offline_subtitle),
                                actionLabel = stringResource(R.string.deeplink_retry),
                                onAction = onRefresh,
                            )
                        }
                    }

                    if (data.isAdminHybrid) {
                        AdminHybridReturnBanner(onClick = onNavigateToHome)
                    }
                }

                if (showCategorySelector) {
                    CategorySelectorBottomSheet(
                        categories = categoryOptions,
                        selectedCategoryId = data.selectedCategory,
                        showItemCount = true,
                        productCountByCategory = data.categoryProductCount,
                        onCategorySelected = { selectedCategory ->
                            when {
                                selectedCategory.isNullOrBlank() -> onClearCategoryFilter()
                                selectedCategory == data.selectedCategory -> onClearCategoryFilter()
                                else -> onCategoryToggle(selectedCategory)
                            }
                            showCategorySelector = false
                        },
                        onDismiss = { showCategorySelector = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun CollapsibleCompanyHeader(
    company: Company,
    visibleFraction: Float,
) {
    val clampedFraction = visibleFraction.coerceIn(0f, 1f)
    if (clampedFraction <= 0f) return

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(220.dp * clampedFraction)
                .clipToBounds()
                .alpha(clampedFraction),
    ) {
        CompanyHeader(
            company = company,
            modifier = Modifier.padding(top = AppDimensions.smallPadding),
        )
    }
}

@Composable
private fun CatalogFilterRow(
    query: String,
    selectedCategory: String?,
    onQueryChange: (String) -> Unit,
    onCategoryClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimensions.mediumPadding,
                    vertical = AppDimensions.smallPadding,
                ),
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholderText = stringResource(R.string.search_products),
            variant = SearchBarVariant.Filled,
            modifier = Modifier.weight(1f),
        )
        CategoryFilterChip(
            selectedCategoryName = selectedCategory,
            onClick = onCategoryClick,
        )
    }
}

@Composable
private fun CatalogTopBar(
    title: String,
    cartCount: Int,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = AppDimensions.smallPadding)
                    .height(AppDimensions.extraLargePadding * 2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.go_back),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onNavigateToCart) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge { Text(text = cartCount.toString()) }
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = stringResource(R.string.cart),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminHybridReturnBanner(
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = AppDimensions.mediumPadding,
                        vertical = AppDimensions.mediumPadding,
                    ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
            Text(
                text = stringResource(R.string.client_catalog_go_my_business),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private val ClientCatalogState.data: ClientCatalogUiData
    get() =
        when (this) {
            is ClientCatalogState.Success -> data
            is ClientCatalogState.Empty -> data
            is ClientCatalogState.Offline -> data
            ClientCatalogState.Loading -> error("Loading state has no data")
            is ClientCatalogState.Error -> error("Error state has no data")
        }

@Preview
@Composable
private fun ClientCatalogScreenPreview() {
    val company = Company(
        id = "1",
        name = "Mi Empresa",
        specialization = "Gestión de catálogos y pedidos",
        address = "Calle Falsa 123",
        businessHours = "09:00 - 18:00",
        whatsappNumber = "123456789",
        whatsappCountryCode = "+54",
        isOwned = true
    )
    val products = listOf(
        ProductEntity(
            id = "1",
            name = "Producto 1",
            price = 100.0,
            companyId = "1",
            categoryName = "Categoria 1"
        ),
        ProductEntity(
            id = "2",
            name = "Producto 2",
            price = 200.0,
            companyId = "1",
            categoryName = "Categoria 2"
        )
    )
    val uiState = ClientCatalogState.Success(
        data = ClientCatalogUiData(
            company = company,
            products = products,
            categories = listOf("Categoria 1", "Categoria 2"),
            categoryProductCount = mapOf("Categoria 1" to 1, "Categoria 2" to 1),
            selectedCategory = null,
            searchQuery = "",
            cartCount = 2,
            isOffline = false,
            isAdminHybrid = false
        )
    )

    MiEmpresaTheme {
        ClientCatalogScreenContent(
            uiState = uiState,
            isRefreshing = false,
            onRefresh = {},
            onNavigateBack = {},
            onNavigateToCart = {},
            onNavigateToHome = {},
            onNavigateToProductDetail = {},
            onSearchQueryChange = {},
            onCategoryToggle = {},
            onClearCategoryFilter = {},
            onClearFilters = {},
            onAddProductToCart = {}
        )
    }
}
