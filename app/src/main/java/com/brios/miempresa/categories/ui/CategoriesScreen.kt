package com.brios.miempresa.categories.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.ItemCard
import com.brios.miempresa.core.ui.components.MessageWithIcon
import com.brios.miempresa.core.ui.components.OfflineBanner
import com.brios.miempresa.core.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateToAddCategory: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCategory) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_category))
            }
        },
    ) { paddingValues ->
        CategoriesContentInternal(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            uiState = uiState,
            searchQuery = searchQuery,
            isRefreshing = isRefreshing,
            isOffline = viewModel.isOffline,
            onRefresh = viewModel::refresh,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onNavigateToCategoryDetail = onNavigateToCategoryDetail,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesContent(
    modifier: Modifier = Modifier,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    CategoriesContentInternal(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        searchQuery = searchQuery,
        isRefreshing = isRefreshing,
        isOffline = viewModel.isOffline,
        onRefresh = viewModel::refresh,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onNavigateToCategoryDetail = onNavigateToCategoryDetail,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriesContentInternal(
    modifier: Modifier = Modifier,
    uiState: CategoriesUiState,
    searchQuery: String,
    isRefreshing: Boolean,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
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
                query = searchQuery,
                onQueryChange = onSearchQueryChanged,
                onSearch = {},
                active = false,
                onActiveChange = {},
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimensions.mediumPadding),
                placeholder = { Text(stringResource(R.string.search_categories)) },
            ) {}

            when (val state = uiState) {
                is CategoriesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is CategoriesUiState.Empty -> {
                    MessageWithIcon(
                        message = stringResource(R.string.no_categories_yet),
                        icon = Icons.Outlined.Category,
                    )
                }
                is CategoriesUiState.Error -> {
                    MessageWithIcon(
                        message = state.message,
                        icon = Icons.Outlined.SearchOff,
                    )
                }
                is CategoriesUiState.Success -> {
                    if (state.categories.isEmpty()) {
                        MessageWithIcon(
                            message = stringResource(R.string.no_categories_match),
                            icon = Icons.Outlined.SearchOff,
                        )
                    } else {
                        // Counter header
                        Text(
                            text = "${state.categories.size} CATEGORÍAS ACTIVAS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier =
                                Modifier.padding(
                                    horizontal = AppDimensions.largePadding,
                                    vertical = AppDimensions.smallPadding,
                                ),
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                                androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = AppDimensions.mediumPadding,
                                ),
                            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                        ) {
                            items(
                                state.categories,
                                key = { it.category.id },
                            ) { item ->
                                val subtitle =
                                    if (item.productCount > 0) {
                                        "${item.productCount} productos"
                                    } else {
                                        stringResource(R.string.no_products_label)
                                    }
                                ItemCard(
                                    title = item.category.name,
                                    subtitle = "${item.category.iconEmoji}  $subtitle",
                                    onClick = {
                                        onNavigateToCategoryDetail(item.category.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
