package com.brios.miempresa.products.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.core.ui.components.CategoryBadge
import com.brios.miempresa.core.ui.components.ItemCard
import com.brios.miempresa.core.ui.components.MessageWithIcon
import com.brios.miempresa.core.ui.components.OfflineBanner
import com.brios.miempresa.core.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    viewModel: ProductsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddProduct) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_product))
            }
        },
    ) { paddingValues ->
        ProductsContentInternal(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            uiState = uiState,
            filters = filters,
            isRefreshing = isRefreshing,
            isOffline = viewModel.isOffline,
            onRefresh = viewModel::refresh,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onPublicFilterChanged = viewModel::onPublicFilterChanged,
            onCategoryFilterChanged = viewModel::onCategoryFilterChanged,
            onClearFilters = viewModel::clearFilters,
            onNavigateToProductDetail = onNavigateToProductDetail,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsContent(
    modifier: Modifier = Modifier,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    viewModel: ProductsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    ProductsContentInternal(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        filters = filters,
        isRefreshing = isRefreshing,
        isOffline = viewModel.isOffline,
        onRefresh = viewModel::refresh,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onPublicFilterChanged = viewModel::onPublicFilterChanged,
        onCategoryFilterChanged = viewModel::onCategoryFilterChanged,
        onClearFilters = viewModel::clearFilters,
        onNavigateToProductDetail = onNavigateToProductDetail,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductsContentInternal(
    modifier: Modifier = Modifier,
    uiState: ProductsUiState,
    filters: ProductFilters,
    isRefreshing: Boolean,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPublicFilterChanged: (PublicFilter) -> Unit,
    onCategoryFilterChanged: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isOffline) {
                OfflineBanner()
            }

            androidx.compose.material3.SearchBar(
                query = filters.searchQuery,
                onQueryChange = onSearchQueryChanged,
                onSearch = {},
                active = false,
                onActiveChange = {},
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimensions.mediumPadding),
                placeholder = { Text(stringResource(R.string.search_products)) },
            ) {}

            when (uiState) {
                is ProductsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ProductsUiState.Empty -> {
                    MessageWithIcon(
                        message = stringResource(R.string.no_products_yet),
                        icon = Icons.Outlined.Inventory2,
                    )
                }
                is ProductsUiState.Error -> {
                    MessageWithIcon(
                        message = uiState.message,
                        icon = Icons.Outlined.SearchOff,
                    )
                }
                is ProductsUiState.EmptyFiltered -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        FilterChipsRow(
                            filters = filters,
                            categories = uiState.categories,
                            onPublicFilterChanged = onPublicFilterChanged,
                            onCategoryFilterChanged = onCategoryFilterChanged,
                        )
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.no_products_match),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
                                TextButton(onClick = onClearFilters) {
                                    Text(stringResource(R.string.clear_filters))
                                }
                            }
                        }
                    }
                }
                is ProductsUiState.Success -> {
                    FilterChipsRow(
                        filters = filters,
                        categories = uiState.categories,
                        onPublicFilterChanged = onPublicFilterChanged,
                        onCategoryFilterChanged = onCategoryFilterChanged,
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            androidx.compose.foundation.layout.PaddingValues(
                                horizontal = AppDimensions.mediumPadding,
                            ),
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
                    ) {
                        items(
                            uiState.products,
                            key = { it.id },
                        ) { product ->
                            val category =
                                uiState.categories
                                    .find { it.id == product.categoryId }
                            ItemCard(
                                title = product.name,
                                subtitle = "$${product.price}",
                                imageUrl = product.localImagePath ?: product.imageUrl,
                                isPublic = product.isPublic,
                                badge =
                                    if (category != null) {
                                        {
                                            CategoryBadge(
                                                emoji = category.iconEmoji,
                                                name = category.name,
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                onEdit = { onNavigateToProductDetail(product.id) },
                                onClick = { onNavigateToProductDetail(product.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    filters: ProductFilters,
    categories: List<Category>,
    onPublicFilterChanged: (PublicFilter) -> Unit,
    onCategoryFilterChanged: (String?) -> Unit,
) {
    LazyRow(
        modifier =
            Modifier.padding(
                horizontal = AppDimensions.mediumPadding,
                vertical = AppDimensions.smallPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
    ) {
        item {
            FilterChip(
                selected = filters.publicFilter == PublicFilter.ALL,
                onClick = { onPublicFilterChanged(PublicFilter.ALL) },
                label = { Text(stringResource(R.string.filter_all)) },
            )
        }
        item {
            FilterChip(
                selected = filters.publicFilter == PublicFilter.PUBLIC,
                onClick = { onPublicFilterChanged(PublicFilter.PUBLIC) },
                label = { Text(stringResource(R.string.filter_public)) },
            )
        }
        item {
            FilterChip(
                selected = filters.publicFilter == PublicFilter.PRIVATE,
                onClick = { onPublicFilterChanged(PublicFilter.PRIVATE) },
                label = { Text(stringResource(R.string.filter_private)) },
            )
        }
        items(categories) { category ->
            AssistChip(
                onClick = {
                    onCategoryFilterChanged(
                        if (filters.categoryId == category.id) null else category.id,
                    )
                },
                label = { Text("${category.iconEmoji} ${category.name}") },
            )
        }
    }
}
