package com.brios.miempresa.catalog.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Badge
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.catalog.ui.components.CatalogProductItem
import com.brios.miempresa.catalog.ui.components.CompanyHeader
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.ui.components.CategoryFilterChip
import com.brios.miempresa.core.ui.components.CategorySelectorBottomSheet
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.NotFoundView
import com.brios.miempresa.core.ui.components.OfflineBanner
import com.brios.miempresa.core.ui.components.SearchBar
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
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = AppDimensions.extraLargePadding * 2),
            )
        },
    ) {
        when (uiState) {
            ClientCatalogState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = stringResource(R.string.client_catalog_loading))
                }
            }

            is ClientCatalogState.Error -> {
                EmptyStateView(
                    icon = Icons.Outlined.SearchOff,
                    title = uiState.message,
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
                val listState = rememberLazyListState()
                val showCollapsedTitle by
                    remember(listState, uiState) {
                        derivedStateOf {
                            uiState is ClientCatalogState.Success &&
                                (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 72)
                        }
                    }
                var showCategorySelector by rememberSaveable(data.company.id) { mutableStateOf(false) }
                val categoryOptions =
                    remember(data.categories, data.company.id) {
                        data.categories.map { rawCategory ->
                            val (emoji, name) = splitCategoryLabel(rawCategory)
                            Category(
                                id = rawCategory,
                                name = name,
                                iconEmoji = emoji,
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

                    when (uiState) {
                        is ClientCatalogState.Success -> {
                            CatalogSuccessContent(
                                data = data,
                                listState = listState,
                                onSearchQueryChange = onSearchQueryChange,
                                onCategoryClick = { showCategorySelector = true },
                                onNavigateToProductDetail = onNavigateToProductDetail,
                                onAddProductToCart = onAddProductToCart,
                            )
                        }

                        is ClientCatalogState.Empty,
                        is ClientCatalogState.Offline,
                        -> {
                            CatalogStaticContent(
                                state = uiState,
                                onSearchQueryChange = onSearchQueryChange,
                                onCategoryClick = { showCategorySelector = true },
                                onRefresh = onRefresh,
                                onClearFilters = onClearFilters,
                            )
                        }
                    }

                    if (data.isAdminHybrid) {
                        AdminHybridReturnBanner(
                            onClick = {
                                onNavigateToHome()
                            },
                        )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnScope.CatalogSuccessContent(
    data: ClientCatalogUiData,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryClick: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onAddProductToCart: (String) -> Unit,
) {
    val productRows = remember(data.products) { data.products.chunked(2) }

    LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f),
        contentPadding =
            PaddingValues(
                bottom =
                    if (data.isAdminHybrid) {
                        AppDimensions.extraLargePadding * 3
                    } else {
                        AppDimensions.largePadding
                    },
            ),
    ) {
        item("company-header") {
            CompanyHeader(
                company = data.company,
                modifier = Modifier.padding(top = AppDimensions.mediumPadding, bottom = AppDimensions.smallPadding),
                )
        }

        stickyHeader("catalog-filters") {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
            ) {
                CatalogFilterRow(
                    query = data.searchQuery,
                    selectedCategory = data.selectedCategory,
                    onQueryChange = onSearchQueryChange,
                    onCategoryClick = onCategoryClick,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        if (data.isOffline) {
            item("offline-banner") { OfflineBanner() }
        }

        items(
            items = productRows,
            key = { row -> row.first().id },
        ) { rowProducts ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimensions.mediumPadding)
                        .padding(top = AppDimensions.mediumPadding),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding)
            ) {
                rowProducts.forEach { product ->
                    CatalogProductItem(
                        product = product,
                        onClick = { onNavigateToProductDetail(product.id) },
                        onAddToCart = { onAddProductToCart(product.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowProducts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.CatalogStaticContent(
    state: ClientCatalogState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryClick: () -> Unit,
    onRefresh: () -> Unit,
    onClearFilters: () -> Unit,
) {
    val data = state.data
    Column(
        modifier = Modifier.weight(1f),
    ) {
        CompanyHeader(
            company = data.company,
            modifier = Modifier.padding(top = AppDimensions.smallPadding),
        )

        CatalogFilterRow(
            query = data.searchQuery,
            selectedCategory = data.selectedCategory,
            onQueryChange = onSearchQueryChange,
            onCategoryClick = onCategoryClick,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (data.isOffline) {
            OfflineBanner()
        }

        when (state) {
            is ClientCatalogState.Empty -> {
                if (state.hasActiveFilters) {
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

            else -> Unit
        }
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
                    vertical = AppDimensions.smallPadding,
                    horizontal = AppDimensions.mediumPadding,
                ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholderText = stringResource(R.string.search_products),
            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = AppDimensions.smallPadding),
        )
        CategoryFilterChip(
            modifier = Modifier.height(AppDimensions.searchBarHeight),
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
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = onNavigateToCart) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = stringResource(R.string.cart),
                    )
                }
                if (cartCount > 0) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
                                .padding(2.dp),
                    ) {
                        Badge {
                            Text(text = cartCount.toString())
                        }
                    }
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

private fun splitCategoryLabel(rawValue: String): Pair<String, String> {
    val trimmed = rawValue.trim()
    val separatorIndex = trimmed.indexOf(' ')
    if (separatorIndex <= 0) {
        return "" to trimmed
    }
    val prefix = trimmed.substring(0, separatorIndex)
    val suffix = trimmed.substring(separatorIndex + 1).trim()
    val looksLikeEmojiPrefix = prefix.length <= 4 && prefix.any { !it.isLetterOrDigit() }
    return if (looksLikeEmojiPrefix && suffix.isNotBlank()) {
        prefix to suffix
    } else {
        "" to trimmed
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
    val company =
        Company(
            id = "1",
            name = "Mi Empresa",
            specialization = "Gestión de catálogos y pedidos",
            address = "Calle Falsa 123",
            businessHours = "09:00 - 18:00",
            whatsappNumber = "123456789",
            whatsappCountryCode = "+54",
            isOwned = true,
        )
    val products =
        listOf(
            ProductEntity(
                id = "1",
                name = "Producto with a very large name that must fit in 3 lines",
                price = 100.0,
                companyId = "1",
                categoryName = "🥤 Categoria 1",
            ),
            ProductEntity(
                id = "2",
                name = "Producto 2",
                price = 200.0,
                companyId = "1",
                categoryName = "🍔 Categoria 2",
            ),
            ProductEntity(
                id = "3",
                name = "Producto 3",
                price = 200.0,
                companyId = "1",
                categoryName = "🍔 Categoria 1",
            ),
        )
    val uiState =
        ClientCatalogState.Success(
            data =
                ClientCatalogUiData(
                    company = company,
                    products = products,
                    categories = listOf("🥤 Categoria 1", "🍔 Categoria 2"),
                    categoryProductCount = mapOf("🥤 Categoria 1" to 1, "🍔 Categoria 2" to 1),
                    selectedCategory = null,
                    searchQuery = "",
                    cartCount = 2,
                    isOffline = false,
                    isAdminHybrid = false,
                ),
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
            onAddProductToCart = {},
        )
    }
}
