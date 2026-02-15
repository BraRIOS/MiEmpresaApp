package com.brios.miempresa.products.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.ui.components.DeleteDialog
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.OfflineBanner
import com.brios.miempresa.core.ui.components.QuantitySelector
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.products.data.ProductEntity
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductDetailEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                ProductDetailEvent.ProductDeleted -> onNavigateBack()
            }
        }
    }

    val successState = uiState as? ProductDetailUiState.Success

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ProductDetailTopBar(
                title = successState?.data?.company?.name ?: stringResource(R.string.product_detail_title),
                onNavigateBack = onNavigateBack,
            )
        },
        bottomBar = {
            val data = successState?.data
            if (data != null && data.mode == ProductDetailMode.CLIENT) {
                ProductDetailClientBottomAction(
                    quantity = data.quantity,
                    price = data.product.price,
                    onAddToCart = viewModel::addToCart,
                )
            }
        },
    ) { innerPadding ->
        when (val state = uiState) {
            ProductDetailUiState.Loading -> {
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

            is ProductDetailUiState.Error -> {
                EmptyStateView(
                    modifier = Modifier.padding(innerPadding),
                    icon = Icons.Outlined.SearchOff,
                    title = state.message,
                    subtitle = "",
                    actionLabel = stringResource(R.string.deeplink_retry),
                    onAction = viewModel::refresh,
                )
            }

            is ProductDetailUiState.Success -> {
                ProductDetailContent(
                    data = state.data,
                    modifier = Modifier.padding(innerPadding),
                    onQuantityChange = viewModel::onQuantityChange,
                    onEdit = { onNavigateToEdit(state.data.product.id) },
                    onDelete = { showDeleteDialog = true },
                )
            }
        }
    }

    if (showDeleteDialog && successState != null) {
        DeleteDialog(
            itemName = successState.data.product.name,
            title = stringResource(R.string.delete_product),
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteProduct()
            },
        )
    }
}

@Composable
private fun ProductDetailTopBar(
    title: String,
    onNavigateBack: () -> Unit,
) {
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
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            modifier = Modifier.padding(start = AppDimensions.extraSmallPadding),
        )
    }
}

@Composable
private fun ProductDetailContent(
    data: ProductDetailUiData,
    onQuantityChange: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = AppDimensions.largePadding),
    ) {
        if (data.showOfflineWarning) {
            OfflineBanner()
            Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
        }

        ProductDetailImage(
            imageUrl = data.product.imageUrl,
            contentDescription = data.product.name,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimensions.mediumPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
        ) {
            Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
            Text(
                text = data.product.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = currencyFormatter.format(data.product.price),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )

            data.product.categoryName
                ?.takeIf { it.isNotBlank() }
                ?.let { category ->
                    Surface(
                        shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = AppDimensions.smallPadding, vertical = AppDimensions.extraSmallPadding),
                        )
                    }
                }

            data.product.description
                ?.takeIf { it.isNotBlank() }
                ?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

            when (data.mode) {
                ProductDetailMode.CLIENT -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.product_detail_quantity),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        QuantitySelector(
                            quantity = data.quantity,
                            onQuantityChange = onQuantityChange,
                            minQuantity = 1,
                            maxQuantity = 99,
                        )
                    }
                }

                ProductDetailMode.ADMIN -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                    ) {
                        Button(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                            Text(text = stringResource(R.string.edit_product))
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                            border = BorderStroke(AppDimensions.smallBorderWidth, SlateGray200),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                            Text(text = stringResource(R.string.delete))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductDetailImage(
    imageUrl: String?,
    contentDescription: String,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = AppDimensions.mediumPadding)
                .clip(RoundedCornerShape(AppDimensions.mediumCornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = { ProductImagePlaceholder() },
            error = { ProductImagePlaceholder() },
        )
    }
}

@Composable
private fun ProductImagePlaceholder() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(AppDimensions.largeIconSize),
        )
    }
}

@Composable
private fun ProductDetailClientBottomAction(
    quantity: Int,
    price: Double,
    onAddToCart: () -> Unit,
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")) }
    val subtotal = quantity * price

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
    ) {
        Button(
            onClick = onAddToCart,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppDimensions.mediumPadding)
                    .height(56.dp),
            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            contentPadding = PaddingValues(horizontal = AppDimensions.mediumPadding),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.product_detail_add_to_cart),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.product_detail_subtotal, currencyFormatter.format(subtotal)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private val previewCompany =
    Company(
        id = "company-1",
        name = "La Tienda de Prueba",
        isOwned = false,
        publicSheetId = "sheet-1",
    )

private val previewProduct =
    ProductEntity(
        id = "product-1",
        name = "Producto de ejemplo",
        price = 12345.0,
        companyId = "company-1",
        description = "Descripción de ejemplo para detalle de producto.",
        categoryName = "Panificados",
        imageUrl = null,
    )

@Preview(showBackground = true)
@Composable
private fun ProductDetailAdminPreview() {
    MiEmpresaTheme {
        ProductDetailContent(
            data =
                ProductDetailUiData(
                    product = previewProduct,
                    company = previewCompany,
                    mode = ProductDetailMode.ADMIN,
                    quantity = 1,
                ),
            onQuantityChange = {},
            onEdit = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailClientPreview() {
    MiEmpresaTheme {
        ProductDetailContent(
            data =
                ProductDetailUiData(
                    product = previewProduct,
                    company = previewCompany,
                    mode = ProductDetailMode.CLIENT,
                    quantity = 2,
                ),
            onQuantityChange = {},
            onEdit = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailClientOfflinePreview() {
    MiEmpresaTheme {
        ProductDetailContent(
            data =
                ProductDetailUiData(
                    product = previewProduct,
                    company = previewCompany,
                    mode = ProductDetailMode.CLIENT,
                    quantity = 1,
                    showOfflineWarning = true,
                ),
            onQuantityChange = {},
            onEdit = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailClientHighQuantityPreview() {
    MiEmpresaTheme {
        ProductDetailContent(
            data =
                ProductDetailUiData(
                    product = previewProduct,
                    company = previewCompany,
                    mode = ProductDetailMode.CLIENT,
                    quantity = 15,
                ),
            onQuantityChange = {},
            onEdit = {},
            onDelete = {},
        )
    }
}
