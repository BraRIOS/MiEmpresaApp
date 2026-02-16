package com.brios.miempresa.products.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.brios.miempresa.R
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.ui.components.CategoryBadge
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
    onNavigateToCart: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
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

    ProductDetailScreenContent(
        uiState = uiState,
        cartCount = cartCount,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onNavigateToCart = {
            successState?.data?.company?.id?.let(onNavigateToCart)
        },
        onEdit = { successState?.data?.product?.id?.let(onNavigateToEdit) },
        onDelete = { showDeleteDialog = true },
        onQuantityChange = viewModel::onQuantityChange,
        onAddToCart = viewModel::addToCart,
        onRefresh = viewModel::refresh,
        modifier = modifier
    )

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
fun ProductDetailScreenContent(
    uiState: ProductDetailUiState,
    cartCount: Int,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val successState = uiState as? ProductDetailUiState.Success
    val data = successState?.data

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ProductDetailTopBar(
                title = stringResource(R.string.product_detail_title),
                onNavigateBack = onNavigateBack,
                showCartAction = data?.mode == ProductDetailMode.CLIENT,
                cartCount = cartCount,
                onNavigateToCart = onNavigateToCart,
            )
        },
        bottomBar = {
            if (data != null && data.mode == ProductDetailMode.CLIENT) {
                ProductDetailClientBottomAction(
                    quantity = data.quantity,
                    price = data.product.price,
                    onQuantityChange = onQuantityChange,
                    onAddToCart = onAddToCart,
                )
            }
        },
    ) { innerPadding ->
        when (uiState) {
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
                    title = uiState.message,
                    subtitle = "",
                    actionLabel = stringResource(R.string.deeplink_retry),
                    onAction = onRefresh,
                )
            }

            is ProductDetailUiState.Success -> {
                ProductDetailContent(
                    data = uiState.data,
                    modifier = Modifier.padding(innerPadding),
                    onEdit = onEdit,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
private fun ProductDetailTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    showCartAction: Boolean,
    cartCount: Int,
    onNavigateToCart: () -> Unit,
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
            modifier =
                Modifier
                    .padding(start = AppDimensions.extraSmallPadding)
                    .weight(1f),
        )
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (showCartAction) {
                IconButton(onClick = onNavigateToCart) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = stringResource(R.string.cart),
                    )
                }
                if (cartCount > 0) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
                                .padding(2.dp),
                    ) {
                        Badge {
                            Text(text = cartCount.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductDetailContent(
    data: ProductDetailUiData,
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
            imageUrl = resolveProductDetailImageSource(data.product),
            contentDescription = data.product.name,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimensions.mediumPadding, vertical = AppDimensions.mediumPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = data.product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                data.product.categoryName
                    ?.takeIf { it.isNotBlank() }
                    ?.let { rawCategory ->
                        val (emoji, name) = splitCategoryLabel(rawCategory)
                        CategoryBadge(
                            emoji = emoji,
                            name = name,
                            onClick = {  },
                        )
                    }
            }

            Text(
                text = currencyFormatter.format(data.product.price),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = AppDimensions.smallPadding),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = stringResource(R.string.description_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

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
                ProductDetailMode.CLIENT -> Unit
                ProductDetailMode.ADMIN -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                    ) {
                        Button(
                            onClick = onEdit,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
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
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
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
                .aspectRatio(4f / 5f)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        val resolvedUrl = imageUrl?.takeIf { it.isNotBlank() }
        if (resolvedUrl == null) {
            ProductImagePlaceholder()
        } else {
            val painter =
                rememberAsyncImagePainter(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(resolvedUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.miempresa_logo_glyph)
                            .error(R.drawable.miempresa_logo_glyph)
                            .fallback(R.drawable.miempresa_logo_glyph)
                            .build(),
                )

            Image(
                painter = painter,
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .blur(radius = 20.dp)
                        .alpha(0.55f),
                contentScale = ContentScale.Crop,
            )
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
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
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")) }
    val subtotal = quantity * price

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppDimensions.mediumPadding)
                    .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
        ) {
            QuantitySelector(
                quantity = quantity,
                onQuantityChange = onQuantityChange,
                minQuantity = 1,
                maxQuantity = 99,
                modifier = Modifier.height(AppDimensions.largeIconSize),
                iconSize = AppDimensions.mediumLargeIconSize,
            )
            Button(
                onClick = onAddToCart,
                modifier = Modifier
                    .weight(1f)
                    .height(AppDimensions.largeIconSize),
                shape = RoundedCornerShape(AppDimensions.largeCornerRadius),
            ) {
                Text(
                    text =
                        stringResource(
                            R.string.product_detail_add_with_subtotal,
                            currencyFormatter.format(subtotal),
                        ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun splitCategoryLabel(rawValue: String): Pair<String, String> {
    val trimmed = rawValue.trim()
    val separatorIndex = trimmed.indexOf(' ')
    if (separatorIndex <= 0) {
        return "" to trimmed
    }
    val prefix = trimmed.substring(0, separatorIndex)
    val suffix = trimmed.substring(separatorIndex + 1).trim()
    val looksLikeEmojiPrefix = prefix.length <= 4 && prefix.any { !it.isLetterOrDigit() }
    return if (looksLikeEmojiPrefix && suffix.isNotBlank()) {
        prefix to suffix
    } else {
        "" to trimmed
    }
}

internal fun resolveProductDetailImageSource(product: ProductEntity): String? {
    return product.localImagePath?.takeIf { it.isNotBlank() } ?: product.imageUrl?.takeIf { it.isNotBlank() }
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
        name = "Producto de ejemplo con nombre bastante largo simulando caracteristicas en el título",
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
        ProductDetailScreenContent(
            uiState = ProductDetailUiState.Success(
                data = ProductDetailUiData(
                    product = previewProduct,
                    company = previewCompany,
                    mode = ProductDetailMode.ADMIN,
                    quantity = 1,
                )
            ),
            cartCount = 0,
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onNavigateToCart = {},
            onEdit = {},
            onDelete = {},
            onQuantityChange = {},
            onAddToCart = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailClientPreview() {
    MiEmpresaTheme {
        ProductDetailScreenContent(
            uiState = ProductDetailUiState.Success(
                data = ProductDetailUiData(
                    product = previewProduct,
                    company = previewCompany,
                    mode = ProductDetailMode.CLIENT,
                    quantity = 2,
                )
            ),
            cartCount = 3,
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onNavigateToCart = {},
            onEdit = {},
            onDelete = {},
            onQuantityChange = {},
            onAddToCart = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailClientOfflinePreview() {
    MiEmpresaTheme {
        ProductDetailScreenContent(
            uiState = ProductDetailUiState.Success(
                data = ProductDetailUiData(
                    product = previewProduct,
                    company = previewCompany,
                    mode = ProductDetailMode.CLIENT,
                    quantity = 1,
                    showOfflineWarning = true,
                )
            ),
            cartCount = 0,
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onNavigateToCart = {},
            onEdit = {},
            onDelete = {},
            onQuantityChange = {},
            onAddToCart = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailClientHighQuantityPreview() {
    MiEmpresaTheme {
        ProductDetailScreenContent(
            uiState = ProductDetailUiState.Success(
                data = ProductDetailUiData(
                    product = previewProduct,
                    company = previewCompany,
                    mode = ProductDetailMode.CLIENT,
                    quantity = 15,
                )
            ),
            cartCount = 5,
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onNavigateToCart = {},
            onEdit = {},
            onDelete = {},
            onQuantityChange = {},
            onAddToCart = {},
            onRefresh = {}
        )
    }
}
