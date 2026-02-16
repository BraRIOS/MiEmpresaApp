package com.brios.miempresa.catalog.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.catalog.ui.components.AddStoreSheet
import com.brios.miempresa.catalog.ui.components.StoreCard
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.MiEmpresaDialog
import com.brios.miempresa.core.ui.components.NotFoundView
import com.brios.miempresa.core.ui.components.SearchBar
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoresScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToCatalog: (String) -> Unit,
    onNavigateToSignIn: () -> Unit = {},
    viewModel: MyStoresViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddStoreSheet by rememberSaveable { mutableStateOf(false) }
    var sheetIdInput by rememberSaveable { mutableStateOf("") }
    var showHelpDialog by rememberSaveable { mutableStateOf(false) }
    var pendingCartReplacement by remember { mutableStateOf<MyStoresEvent.ConfirmCartReplacement?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MyStoresEvent.NavigateToCatalog -> {
                    sheetIdInput = ""
                    showAddStoreSheet = false
                    pendingCartReplacement = null
                    onNavigateToCatalog(event.companyId)
                }
                is MyStoresEvent.ConfirmCartReplacement -> {
                    pendingCartReplacement = event
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
    val showFab = !uiState.isLoading && uiState.stores.isNotEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = AppDimensions.extraSmallPadding),
                    )
                },
                navigationIcon = {
                    if (uiState.isAdminHybridContext) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.go_back),
                            )
                        }
                    }
                },
                actions = {
                    if (!uiState.isAdminHybridContext)
                        IconButton(onClick = { showHelpDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = stringResource(R.string.my_stores_help_icon),
                            )
                        }
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    subtitleContentColor = Color.Unspecified
                ),
                windowInsets = WindowInsets.safeDrawing,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = AppDimensions.smallPadding, vertical = AppDimensions.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {





            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { showAddStoreSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = AppDimensions.largePadding),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.my_stores_add_store_button),
                    )
                }
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
            onNavigateToSignIn = onNavigateToSignIn,
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

    pendingCartReplacement?.let { warning ->
        MiEmpresaDialog(
            title = stringResource(R.string.my_stores_switch_cart_title),
            text = stringResource(R.string.my_stores_switch_cart_message, warning.cartCompanyName),
            confirmLabel = stringResource(R.string.my_stores_switch_cart_confirm),
            dismissLabel = stringResource(R.string.cancel),
            onDismiss = { pendingCartReplacement = null },
            onConfirm = {
                pendingCartReplacement = null
                viewModel.confirmStoreSwitch(
                    targetCompanyId = warning.targetCompanyId,
                    cartCompanyId = warning.cartCompanyId,
                )
            },
        )
    }

    if (showHelpDialog && !uiState.isAdminHybridContext) {
        MiEmpresaDialog(
            title = stringResource(R.string.my_stores_help_title),
            text = stringResource(R.string.my_stores_help_message),
            confirmLabel = stringResource(R.string.my_stores_help_confirm),
            onDismiss = { showHelpDialog = false },
            onConfirm = { showHelpDialog = false },
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
    onNavigateToSignIn: () -> Unit,
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
                if (!uiState.isAdminHybridContext) {
                    MyStoresConversionBanner(
                        onNavigateToSignIn = onNavigateToSignIn,
                    )
                    Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
                }

                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholderText = stringResource(R.string.my_stores_search_placeholder),
                )
                Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

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
                            icon = Icons.Outlined.QrCode,
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
            }
        }
    }
}

@Composable
private fun MyStoresConversionBanner(
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppDimensions.mediumPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
        ) {
            Icon(
                imageVector = Icons.Outlined.Storefront,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.my_stores_conversion_title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onNavigateToSignIn) {
                Text(text = stringResource(R.string.my_stores_conversion_action))
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
            onNavigateToSignIn = {},
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
            onNavigateToSignIn = {},
        )
    }
}
