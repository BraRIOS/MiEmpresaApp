package com.brios.miempresa.categories.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.MiEmpresaDialog
import com.brios.miempresa.core.ui.components.NotFoundView
import com.brios.miempresa.core.ui.components.ItemCard
import com.brios.miempresa.core.ui.components.ItemType
import com.brios.miempresa.core.ui.components.OfflineBanner
import com.brios.miempresa.core.ui.components.SearchBar
import com.brios.miempresa.core.ui.components.TriangleArrowRefreshIndicator
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray400

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
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    CategoriesContentInternal(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        searchQuery = searchQuery,
        isRefreshing = isRefreshing,
        isOffline = isOffline,
        onRefresh = viewModel::refresh,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onDeleteCategory = viewModel::deleteCategory,
        onNavigateToCategoryDetail = onNavigateToCategoryDetail,
        onNavigateToAddCategory = onNavigateToAddCategory,
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
    onDeleteCategory: (String) -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    onNavigateToAddCategory: () -> Unit,
) {
    var itemToDelete by remember { mutableStateOf<Triple<String, String, Int>?>(null) }
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
        Column(modifier = Modifier.fillMaxSize()) {
            if (isOffline) {
                OfflineBanner()
            }

            // Sticky header: SearchBar + counter
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
            ) {
                Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    placeholderText = stringResource(R.string.search_categories),
                    modifier = Modifier.padding(horizontal = AppDimensions.mediumPadding),
                )

                if (uiState is CategoriesUiState.Success && uiState.categories.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.categories_count_label, uiState.categories.size),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray400,
                        modifier =
                            Modifier.padding(
                                horizontal = AppDimensions.largePadding,
                                vertical = AppDimensions.smallPadding,
                            ),
                    )
                }
            }

            // Body content
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
                    EmptyStateView(
                        icon = Icons.Outlined.FolderOpen,
                        title = stringResource(R.string.empty_state_categories),
                        subtitle = stringResource(R.string.empty_state_categories_subtitle),
                        actionLabel = stringResource(R.string.empty_state_categories_action),
                        onAction = onNavigateToAddCategory,
                    )
                }
                is CategoriesUiState.Error -> {
                    EmptyStateView(
                        icon = Icons.Outlined.SearchOff,
                        title = state.message,
                        subtitle = "",
                    )
                }
                is CategoriesUiState.Success -> {
                    if (state.categories.isEmpty()) {
                        NotFoundView(
                            message = stringResource(R.string.no_categories_match),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                                androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = AppDimensions.mediumPadding,
                                    vertical = AppDimensions.smallPadding,
                                ),
                            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                        ) {
                            items(
                                state.categories,
                                key = { it.category.id },
                            ) { item ->
                                ItemCard(
                                    title = item.category.name,
                                    subtitle = "",
                                    emojiIcon = item.category.iconEmoji,
                                    productCount = item.productCount,
                                    itemType = ItemType.CATEGORY,
                                    onDelete = { itemToDelete = Triple(item.category.id, item.category.name, item.productCount) },
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

    itemToDelete?.let { (id, name, productCount) ->
        MiEmpresaDialog(
            title = stringResource(R.string.delete_category),
            text = if (productCount > 0) {
                stringResource(R.string.category_has_products, productCount)
            } else {
                stringResource(R.string.delete_dialog_title) + "\"$name\"?"
            },
            confirmLabel = stringResource(R.string.delete),
            dismissLabel = stringResource(R.string.cancel),
            confirmEnabled = productCount == 0,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                onDeleteCategory(id)
                itemToDelete = null
            },
        )
    }
}

@Preview
@Composable
private fun CategoriesContentInternalPreview() {
    MiEmpresaTheme {
        CategoriesContentInternal(
            uiState =
                CategoriesUiState.Success(
                    categories =
                        listOf(
                            CategoryWithCount(
                                category =
                                    Category(
                                        id = "1",
                                        name = "Bebidas",
                                        iconEmoji = "🥤",
                                        companyId = "1",
                                    ),
                                productCount = 10,
                            ),
                            CategoryWithCount(
                                category =
                                    Category(
                                        id = "2",
                                        name = "Snacks",
                                        iconEmoji = "🍿",
                                        companyId = "1",
                                    ),
                                productCount = 5,
                            ),
                            CategoryWithCount(
                                category =
                                    Category(
                                        id = "3",
                                        name = "No Icon",
                                        iconEmoji = "",
                                        companyId = "1",
                                    ),
                                productCount = 0,
                            ),
                        ),
                ),
            searchQuery = "",
            isRefreshing = false,
            isOffline = false,
            onRefresh = {},
            onSearchQueryChanged = {},
            onDeleteCategory = {},
            onNavigateToCategoryDetail = {},
            onNavigateToAddCategory = {},
        )
    }
}
