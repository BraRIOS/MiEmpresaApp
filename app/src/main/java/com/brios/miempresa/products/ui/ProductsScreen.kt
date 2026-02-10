package com.brios.miempresa.products.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.core.ui.components.CategoryBadge
import com.brios.miempresa.core.ui.components.DeleteDialog
import com.brios.miempresa.core.ui.components.CategorySelectorBottomSheet
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.ItemCard
import com.brios.miempresa.core.ui.components.OfflineBanner
import com.brios.miempresa.core.ui.components.SearchBar
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.products.data.ProductEntity

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
    var itemToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
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
                                onDelete = { itemToDelete = product.id to product.name },
                                onClick = { onNavigateToProductDetail(product.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { (id, name) ->
        DeleteDialog(
            itemName = name,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                onDeleteProduct(id)
                itemToDelete = null
            },
        )
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
    Row(
        modifier =
            Modifier
                .padding(
                    horizontal = AppDimensions.mediumPadding,
                    vertical = AppDimensions.smallPadding,
                )
                .height(IntrinsicSize.Min)
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
    ) {
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
                    text = selectedCategoryName ?: "Categoría",
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    stringResource(R.string.category_filter_description),
                )
            },
            colors = AssistChipDefaults.assistChipColors().copy(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                trailingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            border = AssistChipDefaults.assistChipBorder(
                enabled = true,
                borderColor = MaterialTheme.colorScheme.outline
            ),
            shape = ShapeDefaults.Large
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight().padding(vertical = AppDimensions.mediumSmallPadding),
            thickness = 2.dp,
        )

        VisibilityFilterChip(
            selected = filters.publicFilter == PublicFilter.ALL,
            onClick = { onPublicFilterChanged(PublicFilter.ALL) },
            label = { Text(stringResource(R.string.filter_all)) },
        )

        VisibilityFilterChip(
            selected = filters.publicFilter == PublicFilter.PUBLIC,
            onClick = { onPublicFilterChanged(PublicFilter.PUBLIC) },
            label = { Text(stringResource(R.string.filter_public)) },
        )

        VisibilityFilterChip(
            selected = filters.publicFilter == PublicFilter.PRIVATE,
            onClick = { onPublicFilterChanged(PublicFilter.PRIVATE) },
            label = { Text(stringResource(R.string.filter_private)) },
        )
    }
}

@Composable
private fun VisibilityFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        colors = FilterChipDefaults.filterChipColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            selectedContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLabelColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        shape = ShapeDefaults.Large
    )
}


@Preview(showBackground = true)
@Composable
private fun ProductsContentInternalPreview() {
    val sampleCategories = listOf(
        Category(id = "1", name = "Bebidas", iconEmoji = "🥤", companyId = "1"),
        Category(id = "2", name = "Comida", iconEmoji = "🍔", companyId = "1"),
    )

    val sampleProducts = listOf(
        ProductEntity(
            id = "1",
            name = "Coca Cola",
            price = 1500.0,
            companyId = "1",
            categoryId = "1",
            isPublic = true,
        ),
        ProductEntity(
            id = "2",
            name = "Hamburguesa",
            price = 5500.0,
            companyId = "1",
            categoryId = "2",
            isPublic = false,
        ),
    )

    MiEmpresaTheme {
        ProductsContentInternal(
            uiState = ProductsUiState.Success(
                products = sampleProducts,
                categories = sampleCategories,
            ),
            filters = ProductFilters(),
            isRefreshing = false,
            isOffline = false,
            onRefresh = {},
            onSearchQueryChanged = {},
            onPublicFilterChanged = {},
            onCategoryFilterChanged = {},
            onClearFilters = {},
            onDeleteProduct = {},
            onToggleVisibility = { _, _ -> },
            onNavigateToProductDetail = {},
            onNavigateToAddProduct = {},
        )
    }
}
