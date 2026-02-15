package com.brios.miempresa.catalog.ui

import android.text.format.DateUtils
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.catalog.ui.components.AddStoreSheet
import com.brios.miempresa.catalog.ui.components.StoreCard
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.NotFoundView
import com.brios.miempresa.core.ui.components.SearchBar
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun MyStoresScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCatalog: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyStoresViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddStoreSheet by rememberSaveable { mutableStateOf(false) }
    var sheetIdInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MyStoresEvent.NavigateToCatalog -> {
                    sheetIdInput = ""
                    showAddStoreSheet = false
                    onNavigateToCatalog(event.companyId)
                }
                is MyStoresEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val title =
        if (uiState.isAdminHybridContext) {
            stringResource(R.string.visited_stores)
        } else {
            stringResource(R.string.my_stores)
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = AppDimensions.smallPadding, vertical = AppDimensions.smallPadding),
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
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = AppDimensions.extraSmallPadding),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStoreSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.my_stores_add_store_button),
                )
            }
        },
    ) { innerPadding ->
        MyStoresContent(
            uiState = uiState,
            modifier = Modifier.padding(innerPadding),
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onStoreClick = viewModel::openStore,
            onAddByCode = { showAddStoreSheet = true },
            onClearSearch = viewModel::clearSearch,
            onNavigateToHome = onNavigateToHome,
        )
    }

    if (showAddStoreSheet) {
        AddStoreSheet(
            sheetId = sheetIdInput,
            isSubmitting = uiState.isAddingStore,
            onSheetIdChange = { sheetIdInput = it },
            onSubmit = {
                viewModel.addStoreBySheetId(sheetIdInput)
            },
            onDismiss = {
                sheetIdInput = ""
                showAddStoreSheet = false
            },
        )
    }
}

@Composable
private fun MyStoresContent(
    uiState: MyStoresUiState,
    onSearchQueryChange: (String) -> Unit,
    onStoreClick: (String) -> Unit,
    onAddByCode: () -> Unit,
    onClearSearch: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(horizontal = AppDimensions.mediumPadding),
            ) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholderText = stringResource(R.string.my_stores_search_placeholder),
                )
                Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

                when {
                    uiState.filteredStores.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = AppDimensions.largePadding),
                            verticalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                        ) {
                            items(
                                items = uiState.filteredStores,
                                key = { it.id },
                            ) { company ->
                                StoreCard(
                                    company = company,
                                    lastVisitLabel =
                                        stringResource(
                                            R.string.my_stores_last_visit,
                                            formatRelativeLastVisit(company.lastVisited),
                                        ),
                                    onClick = { onStoreClick(company.id) },
                                )
                            }
                        }
                    }

                    uiState.stores.isEmpty() -> {
                        EmptyStateView(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Store,
                            title = stringResource(R.string.my_stores),
                            subtitle = stringResource(R.string.my_stores_placeholder_subtitle),
                            actionLabel = stringResource(R.string.my_stores_add_by_code),
                            onAction = onAddByCode,
                        )
                    }

                    else -> {
                        NotFoundView(
                            modifier = Modifier.weight(1f),
                            message = stringResource(R.string.my_stores_no_results),
                            onAction = onClearSearch,
                        )
                    }
                }

                if (uiState.isAdminHybridContext) {
                    TextButton(
                        onClick = onNavigateToHome,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(R.string.my_stores_back_to_business))
                    }
                }
            }
        }
    }
}

private fun formatRelativeLastVisit(lastVisited: Long?): CharSequence {
    val timestamp = lastVisited ?: return "-"
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    )
}

@Preview(showBackground = true)
@Composable
private fun MyStoresScreenClientEmptyPreview() {
    MiEmpresaTheme {
        MyStoresContent(
            uiState = MyStoresUiState(isLoading = false, isAdminHybridContext = false),
            onSearchQueryChange = {},
            onStoreClick = {},
            onAddByCode = {},
            onClearSearch = {},
            onNavigateToHome = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyStoresScreenAdminEmptyPreview() {
    MiEmpresaTheme {
        MyStoresContent(
            uiState = MyStoresUiState(isLoading = false, isAdminHybridContext = true),
            onSearchQueryChange = {},
            onStoreClick = {},
            onAddByCode = {},
            onClearSearch = {},
            onNavigateToHome = {},
        )
    }
}
