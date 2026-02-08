package com.brios.miempresa.products.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.core.data.local.entities.Category
import com.brios.miempresa.core.data.local.entities.ProductEntity
import com.brios.miempresa.core.ui.components.MessageWithIcon
import com.brios.miempresa.core.ui.components.OfflineBanner

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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (viewModel.isOffline) {
                    OfflineBanner()
                }

                androidx.compose.material3.SearchBar(
                    query = filters.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    placeholder = { Text(stringResource(R.string.search_products)) },
                ) {}

                when (val state = uiState) {
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
                            message = state.message,
                            icon = Icons.Outlined.SearchOff,
                        )
                    }
                    is ProductsUiState.EmptyFiltered -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            FilterChipsRow(
                                filters = filters,
                                categories = state.categories,
                                onPublicFilterChanged = viewModel::onPublicFilterChanged,
                                onCategoryFilterChanged = viewModel::onCategoryFilterChanged,
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
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = viewModel::clearFilters) {
                                        Text(stringResource(R.string.clear_filters))
                                    }
                                }
                            }
                        }
                    }
                    is ProductsUiState.Success -> {
                        FilterChipsRow(
                            filters = filters,
                            categories = state.categories,
                            onPublicFilterChanged = viewModel::onPublicFilterChanged,
                            onCategoryFilterChanged = viewModel::onCategoryFilterChanged,
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                state.products,
                                key = { it.id },
                            ) { product ->
                                ProductCard(
                                    product = product,
                                    categoryName =
                                        state.categories
                                            .find { it.id == product.categoryId }
                                            ?.name,
                                    onClick = { onNavigateToProductDetail(product.id) },
                                )
                            }
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
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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

@Composable
private fun ProductCard(
    product: ProductEntity,
    categoryName: String?,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onClick),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (product.imageUrl != null || product.localImagePath != null) {
                AsyncImage(
                    model = product.localImagePath ?: product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (categoryName != null) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text =
                    if (product.isPublic) {
                        stringResource(R.string.public_label)
                    } else {
                        stringResource(R.string.private_label)
                    },
                style = MaterialTheme.typography.labelSmall,
                color =
                    if (product.isPublic) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}
