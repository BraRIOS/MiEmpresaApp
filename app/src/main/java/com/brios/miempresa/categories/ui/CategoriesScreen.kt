package com.brios.miempresa.categories.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.MessageWithIcon
import com.brios.miempresa.core.ui.components.OfflineBanner

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
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
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
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(
                                    state.categories,
                                    key = { it.category.id },
                                ) { item ->
                                    CategoryCard(
                                        item = item,
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
}

@Composable
private fun CategoryCard(
    item: CategoryWithCount,
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
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.category.iconEmoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.category.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Badge {
                Text(text = item.productCount.toString())
            }
        }
    }
}
