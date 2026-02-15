package com.brios.miempresa.cart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.cart.domain.CartEvent
import com.brios.miempresa.cart.domain.CartUiState
import com.brios.miempresa.cart.ui.components.CartItemCard
import com.brios.miempresa.cart.ui.components.CartSummary
import com.brios.miempresa.core.ui.components.EmptyStateView
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
    val checkoutPendingMessage = stringResource(R.string.cart_checkout_pending)
    val cartClearedMessage = stringResource(R.string.cart_cleared)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CartEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is CartEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                CartEvent.NavigateToCheckout -> snackbarHostState.showSnackbar(checkoutPendingMessage)
                CartEvent.CartCleared -> snackbarHostState.showSnackbar(cartClearedMessage)
            }
        }
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
                    onCheckout = viewModel::checkout,
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
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
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
                        CartItemCard(
                            item = item,
                            onQuantityChange = { quantity -> viewModel.updateQuantity(item.id, quantity) },
                            onRemove = { viewModel.removeItem(item.id) },
                        )
                    }
                }
            }
        }
    }
}
