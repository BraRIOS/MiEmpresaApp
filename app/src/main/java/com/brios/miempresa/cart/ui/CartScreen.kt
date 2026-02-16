package com.brios.miempresa.cart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.cart.domain.CartEvent
import com.brios.miempresa.cart.domain.CartUiState
import com.brios.miempresa.cart.domain.PriceValidationResult
import com.brios.miempresa.cart.domain.WhatsAppHelper
import com.brios.miempresa.cart.ui.components.CartSummary
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.MiEmpresaDialog
import com.brios.miempresa.core.ui.components.OrderProductListItem
import com.brios.miempresa.core.ui.components.OrderProductPriceChange
import com.brios.miempresa.core.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val validatingMessage = stringResource(R.string.cart_validating_prices)
    val cartClearedMessage = stringResource(R.string.cart_cleared)
    val whatsappNotInstalledMessage = stringResource(R.string.cart_whatsapp_not_installed)

    var pendingConfirmationOnReturn by rememberSaveable { mutableStateOf(false) }
    var showOrderSentDialog by rememberSaveable { mutableStateOf(false) }
    var hasHandledFirstResume by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.validateOnEnter()
        viewModel.events.collect { event ->
            when (event) {
                is CartEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is CartEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                CartEvent.CartCleared -> snackbarHostState.showSnackbar(cartClearedMessage)
                is CartEvent.ProceedToWhatsApp -> {
                    val opened = WhatsAppHelper.openChat(context, event.phoneNumber, event.message)
                    if (opened) {
                        pendingConfirmationOnReturn = true
                    } else {
                        snackbarHostState.showSnackbar(whatsappNotInstalledMessage)
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, pendingConfirmationOnReturn) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    if (hasHandledFirstResume) {
                        viewModel.validateOnEnter()
                    } else {
                        hasHandledFirstResume = true
                    }
                    if (pendingConfirmationOnReturn) {
                        showOrderSentDialog = true
                        pendingConfirmationOnReturn = false
                    }
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                    text = stringResource(R.string.cart_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        bottomBar = {
            val state = uiState as? CartUiState.Success
            if (state != null) {
                CartSummary(
                    totalItems = state.totalItems,
                    totalPrice = state.totalPrice,
                    onCheckout = viewModel::validateAndCheckout,
                    enabled = !state.blocked,
                )
            }
        },
    ) { innerPadding ->
        when (val state = uiState) {
            CartUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            CartUiState.Validating -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = validatingMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            CartUiState.Empty -> {
                EmptyStateView(
                    modifier = Modifier.padding(innerPadding),
                    icon = Icons.Filled.ShoppingCart,
                    title = stringResource(R.string.cart_empty_title),
                    subtitle = stringResource(R.string.cart_empty_subtitle),
                    actionLabel = stringResource(R.string.cart_explore_catalog),
                    onAction = onNavigateToCatalog,
                )
            }

            is CartUiState.Error -> {
                EmptyStateView(
                    modifier = Modifier.padding(innerPadding),
                    icon = Icons.Outlined.SearchOff,
                    title = state.message,
                    subtitle = stringResource(R.string.cart_error_subtitle),
                    actionLabel = stringResource(R.string.cart_explore_catalog),
                    onAction = onNavigateToCatalog,
                )
            }

            is CartUiState.Success -> {
                val priceChangesByProductId =
                    (state.validationResult as? PriceValidationResult.PricesUpdated)
                        ?.changes
                        ?.associateBy { it.productId }
                        .orEmpty()
                val unavailableProducts =
                    (state.validationResult as? PriceValidationResult.ItemsUnavailable)
                        ?.unavailableProducts
                        ?.associateBy { it.productId }
                        .orEmpty()

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                ) {
                    when (val validation = state.validationResult) {
                        is PriceValidationResult.PricesUpdated -> {
                            ValidationBanner(
                                icon = Icons.Filled.Info,
                                message = stringResource(R.string.cart_banner_prices_updated),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                        is PriceValidationResult.ItemsUnavailable -> {
                            ValidationBanner(
                                icon = Icons.Outlined.Inventory2,
                                message =
                                    stringResource(
                                        R.string.cart_banner_items_unavailable,
                                        validation.unavailableProducts.size,
                                    ),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                        PriceValidationResult.Blocked -> {
                            ValidationBanner(
                                icon = Icons.Outlined.WifiOff,
                                message = stringResource(R.string.cart_banner_offline_blocked),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                actionLabel = stringResource(R.string.deeplink_retry),
                                onAction = viewModel::retryValidation,
                            )
                        }
                        null,
                        PriceValidationResult.AllValid,
                        -> Unit
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding =
                            PaddingValues(
                                horizontal = AppDimensions.mediumPadding,
                                vertical = AppDimensions.smallPadding,
                            ),
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                    ) {
                        items(
                        items = state.items,
                        key = { it.id },
                    ) { item ->
                            OrderProductListItem(
                                name = item.productName,
                                price = item.productPrice,
                                quantity = item.quantity,
                                imageUrl = item.productImageUrl,
                                onQuantityChange = { quantity -> viewModel.updateQuantity(item.id, quantity) },
                                onRemove = { viewModel.removeItem(item.id) },
                                priceChange =
                                    priceChangesByProductId[item.productId]?.let { change ->
                                        OrderProductPriceChange(
                                            oldPrice = change.oldPrice,
                                            newPrice = change.newPrice,
                                        )
                                    },
                                unavailableLabel =
                                    if (unavailableProducts.containsKey(item.productId)) {
                                        stringResource(R.string.cart_item_unavailable)
                                    } else {
                                        null
                                    },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showOrderSentDialog) {
        MiEmpresaDialog(
            title = stringResource(R.string.cart_order_sent_title),
            text = stringResource(R.string.cart_order_sent_message),
            confirmLabel = stringResource(R.string.cart_order_sent_confirm),
            dismissLabel = stringResource(R.string.cart_order_sent_dismiss),
            onDismiss = { showOrderSentDialog = false },
            onConfirm = {
                showOrderSentDialog = false
                viewModel.clearCart()
            },
        )
    }
}

@Composable
private fun ValidationBanner(
    icon: ImageVector,
    message: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppDimensions.mediumPadding,
                        vertical = AppDimensions.smallPadding,
                    ),
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                modifier = Modifier.weight(1f),
            )
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        color = contentColor,
                    )
                }
            }
        }
    }
}
