package com.brios.miempresa.products.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.core.ui.components.CategoryBadge
import com.brios.miempresa.core.ui.components.CategorySelectorBottomSheet
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.ItemCard
import com.brios.miempresa.core.ui.components.OfflineBanner
import com.brios.miempresa.core.ui.components.SearchBar
import com.brios.miempresa.core.ui.components.SearchBarVariant
import com.brios.miempresa.core.ui.theme.AppDimensions

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
        onDeleteProduct = viewModel::deleteProduct,
        onToggleVisibility = viewModel::togglePublic,
        onNavigateToProductDetail = onNavigateToProductDetail,
        onNavigateToAddProduct = onNavigateToAddProduct,
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
    onDeleteProduct: (String) -> Unit,
    onToggleVisibility: (String, Boolean) -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToAddProduct: () -> Unit,
) {
    var showCategorySelector by remember { mutableStateOf(false) }
    val allCategories =
        when (uiState) {
            is ProductsUiState.Success -> uiState.categories
            is ProductsUiState.EmptyFiltered -> uiState.categories
            else -> emptyList()
        }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isOffline) {
                OfflineBanner()
            }

            // Sticky header: SearchBar + FilterChips
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
            ) {
                Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

                SearchBar(
                    query = filters.searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    placeholderText = stringResource(R.string.search_products),
                    variant = SearchBarVariant.Outlined,
                )

                when (uiState) {
                    is ProductsUiState.Success, is ProductsUiState.EmptyFiltered -> {
                        FilterChipsRow(
                            filters = filters,
                            selectedCategoryName = allCategories
                                .find { it.id == filters.categoryId }
                                ?.let { "${it.iconEmoji} ${it.name}" },
                            onPublicFilterChanged = onPublicFilterChanged,
                            onShowCategorySelector = { showCategorySelector = true },
                            onClearCategoryFilter = { onCategoryFilterChanged(null) },
                        )
                    }
                    else -> {}
                }

                HorizontalDivider()
            }

            // Body content
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
                    EmptyStateView(
                        icon = Icons.Outlined.ShoppingBag,
                        title = stringResource(R.string.empty_state_products),
                        subtitle = stringResource(R.string.empty_state_products_subtitle),
                        actionLabel = stringResource(R.string.empty_state_products_action),
                        onAction = onNavigateToAddProduct,
                    )
                }
                is ProductsUiState.Error -> {
                    EmptyStateView(
                        icon = Icons.Outlined.SearchOff,
                        title = uiState.message,
                        subtitle = "",
                    )
                }
                is ProductsUiState.EmptyFiltered -> {
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
                is ProductsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            androidx.compose.foundation.layout.PaddingValues(
                                horizontal = AppDimensions.mediumPadding,
                                vertical = AppDimensions.smallPadding,
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
                                onToggleVisibility = {
                                    onToggleVisibility(product.id, !product.isPublic)
                                },
                                onDelete = { onDeleteProduct(product.id) },
                                onClick = { onNavigateToProductDetail(product.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    // CategorySelector bottom sheet
    if (showCategorySelector) {
        CategorySelectorBottomSheet(
            categories = allCategories,
            selectedCategoryId = filters.categoryId,
            onCategorySelected = { categoryId ->
                onCategoryFilterChanged(categoryId)
                showCategorySelector = false
            },
            onDismiss = { showCategorySelector = false },
        )
    }
}

@Composable
private fun FilterChipsRow(
    filters: ProductFilters,
    selectedCategoryName: String?,
    onPublicFilterChanged: (PublicFilter) -> Unit,
    onShowCategorySelector: () -> Unit,
    onClearCategoryFilter: () -> Unit,
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
        item {
            AssistChip(
                onClick = {
                    if (filters.categoryId != null) {
                        onClearCategoryFilter()
                    } else {
                        onShowCategorySelector()
                    }
                },
                label = {
                    Text(
                        text = if (selectedCategoryName != null) {
                            "Categoría: $selectedCategoryName"
                        } else {
                            "Categoría: Todas"
                        },
                    )
                },
            )
        }
    }
}
