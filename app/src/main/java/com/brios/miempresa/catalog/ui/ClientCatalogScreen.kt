package com.brios.miempresa.catalog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.catalog.ui.components.CatalogProductCard
import com.brios.miempresa.catalog.ui.components.CompanyHeader
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.NotFoundView
import com.brios.miempresa.core.ui.components.OfflineBanner
import com.brios.miempresa.core.ui.components.SearchBar
import com.brios.miempresa.core.ui.components.TriangleArrowRefreshIndicator
import com.brios.miempresa.core.ui.theme.AppDimensions

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
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refreshCatalog,
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
                val message = (uiState as ClientCatalogState.Error).message
                EmptyStateView(
                    icon = Icons.Outlined.SearchOff,
                    title = message,
                    subtitle = "",
                    actionLabel = stringResource(R.string.deeplink_retry),
                    onAction = viewModel::refreshCatalog,
                )
            }

            is ClientCatalogState.Success,
            is ClientCatalogState.Empty,
            is ClientCatalogState.Offline,
            -> {
                val data = uiState.data
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CatalogTopBar(
                        title = data.company.name,
                        cartCount = data.cartCount,
                        onNavigateBack = onNavigateBack,
                        onNavigateToCart = { onNavigateToCart(data.company.id) },
                    )

                    if (data.isOffline) {
                        OfflineBanner()
                    }

                    CompanyHeader(company = data.company)

                    SearchBar(
                        query = data.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        placeholderText = stringResource(R.string.search_products),
                    )

                    Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = AppDimensions.mediumPadding),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                    ) {
                        item {
                            FilterChip(
                                selected = data.selectedCategory == null,
                                onClick = viewModel::clearCategoryFilter,
                                label = { Text(text = stringResource(R.string.all_categories)) },
                            )
                        }
                        items(data.categories.size) { index ->
                            val category = data.categories[index]
                            FilterChip(
                                selected = category == data.selectedCategory,
                                onClick = { viewModel.onCategoryToggle(category) },
                                label = { Text(text = category) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

                    when (val state = uiState) {
                        is ClientCatalogState.Success -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                                horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                                contentPadding =
                                    PaddingValues(
                                        start = AppDimensions.mediumPadding,
                                        end = AppDimensions.mediumPadding,
                                        top = AppDimensions.smallPadding,
                                        bottom = AppDimensions.largePadding,
                                    ),
                            ) {
                                items(
                                    items = state.data.products,
                                    key = { it.id },
                                ) { product ->
                                    CatalogProductCard(
                                        product = product,
                                        onClick = { onNavigateToProductDetail(product.id) },
                                        onAddToCart = { viewModel.addProductToCart(product.id) },
                                    )
                                }
                            }
                        }

                        is ClientCatalogState.Empty -> {
                            if (state.hasActiveFilters) {
                                NotFoundView(
                                    modifier = Modifier.weight(1f),
                                    message = stringResource(R.string.client_catalog_empty_filtered),
                                    onAction = viewModel::clearFilters,
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
                                onAction = viewModel::refreshCatalog,
                            )
                        }

                        else -> Unit
                    }

                    if (data.isAdminHybrid) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            tonalElevation = AppDimensions.extraSmallPadding,
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = AppDimensions.mediumPadding,
                                            vertical = AppDimensions.smallPadding,
                                        ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.client_catalog_admin_hybrid_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(onClick = onNavigateToHome) {
                                    Text(text = stringResource(R.string.client_catalog_go_my_business))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogTopBar(
    title: String,
    cartCount: Int,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimensions.smallPadding)
                .height(AppDimensions.extraLargePadding * 2),
        horizontalArrangement = Arrangement.SpaceBetween,
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

private val ClientCatalogState.data: ClientCatalogUiData
    get() =
        when (this) {
            is ClientCatalogState.Success -> data
            is ClientCatalogState.Empty -> data
            is ClientCatalogState.Offline -> data
            ClientCatalogState.Loading -> error("Loading state has no data")
            is ClientCatalogState.Error -> error("Error state has no data")
        }
