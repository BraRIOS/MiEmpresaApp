package com.brios.miempresa.catalog.ui

import android.text.format.DateUtils
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.catalog.ui.components.AddStoreSheet
import com.brios.miempresa.catalog.ui.components.StoreCard
import com.brios.miempresa.core.data.local.entities.Company
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
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { showAddStoreSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 88.dp),
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
                        Spacer(Modifier.weight(if (!uiState.isAdminHybridContext) 0.3f else 0.2f))
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
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface,
                    )
                )
            )
        ) {
            // Decorative background elements
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 62.dp, y = (-62).dp)
                    .size(160.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 20.dp)
                    .rotate(12f)
                    .size(110.dp)
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Box(modifier = Modifier.padding(10.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.my_stores_conversion_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.my_stores_conversion_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Button(
                    onClick = onNavigateToSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.my_stores_conversion_action),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
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

@Preview(showBackground = true)
@Composable
private fun MyStoresScreenPopulatedPreview() {
    val companies = listOf(
        Company(
            id = "1",
            name = "Panadería Los Andes",
            address = "Av. San Martín 402, Mendoza",
            lastVisited = System.currentTimeMillis() - 7200000,
            logoUrl = null
        ),
        Company(
            id = "2",
            name = "Mercado Central",
            address = "Calle Falsa 123",
            lastVisited = System.currentTimeMillis() - 86400000,
            logoUrl = null
        ),
        Company(
            id = "3",
            name = "Patitas Felices",
            specialization = "Accesorios y alimento",
            lastVisited = System.currentTimeMillis() - 172800000,
            logoUrl = null
        )
    )

    MiEmpresaTheme {
        MyStoresContent(
            uiState = MyStoresUiState(
                isLoading = false,
                isAdminHybridContext = false,
                stores = companies,
                filteredStores = companies
            ),
            onSearchQueryChange = {},
            onStoreClick = {},
            onAddByCode = {},
            onClearSearch = {},
            onNavigateToSignIn = {},
        )
    }
}
