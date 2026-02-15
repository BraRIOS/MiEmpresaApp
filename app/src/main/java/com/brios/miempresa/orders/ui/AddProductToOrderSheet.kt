package com.brios.miempresa.orders.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.QuantitySelector
import com.brios.miempresa.core.ui.components.SearchBar
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.products.data.ProductEntity
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductToOrderSheet(
    products: List<ProductEntity>,
    onProductSelected: (ProductEntity, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedProductId by rememberSaveable { mutableStateOf<String?>(null) }
    var quantity by rememberSaveable { mutableIntStateOf(1) }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimensions.largePadding)
                .padding(bottom = AppDimensions.extraLargePadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.order_add_product_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(AppDimensions.defaultIconSize)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_close),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholderText = stringResource(R.string.productSearch),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    val isSelected = product.id == selectedProductId

                    ProductItem(
                        product = product,
                        isSelected = isSelected,
                        quantity = if (isSelected) quantity else 1,
                        onSelect = {
                            if (selectedProductId != product.id) {
                                selectedProductId = product.id
                                quantity = 1
                            }
                        },
                        onQuantityChange = { quantity = it },
                        onAdd = {
                            onProductSelected(product, quantity)
                            selectedProductId = null
                            quantity = 1
                        },
                        onCancel = {
                            selectedProductId = null
                            quantity = 1
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: ProductEntity,
    isSelected: Boolean,
    quantity: Int,
    onSelect: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onAdd: () -> Unit,
    onCancel: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Card(
            onClick = onSelect,
            shape = RoundedCornerShape(AppDimensions.largeCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else SlateGray200),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.mediumPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductIcon(imageUrl = product.imageUrl)

                    Spacer(modifier = Modifier.width(AppDimensions.mediumPadding))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = if (isSelected) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        product.description?.let { desc ->
                            if (desc.isNotBlank()) {
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = if (isSelected) 3 else 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(AppDimensions.smallPadding))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = currencyFormat.format(product.price),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

                        if (!isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp).clickable { onSelect() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Seleccionar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (isSelected) {
                    Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius)
                            )
                            .padding(AppDimensions.mediumPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CANTIDAD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray400,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        QuantitySelector(
                            quantity = quantity,
                            onQuantityChange = onQuantityChange,
                            modifier = Modifier.height(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                            border = BorderStroke(1.dp, SlateGray200),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SlateGray400
                            )
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = onAdd,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Agregar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductIcon(imageUrl: String?) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SlateGray200.copy(alpha = 0.3f),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = null,
                    tint = SlateGray400
                )
            }
        }
    }
}

@Preview
@Composable
fun AddProductToOrderSheetPreview() {
    MiEmpresaTheme {
        val sampleProducts = listOf(
            ProductEntity(
                id = "1",
                name = "Producto 1",
                price = 100.0,
                companyId = "company1",
                description = "Descripción del producto 1"
            ),
            ProductEntity(
                id = "2",
                name = "Producto 2",
                price = 200.0,
                companyId = "company1",
                description = "Descripción del producto 2"
            ),
            ProductEntity(
                id = "3",
                name = "Producto 3",
                price = 300.0,
                companyId = "company1",
                description = "Descripción del producto 3"
            )
        )
        AddProductToOrderSheet(
            products = sampleProducts,
            onProductSelected = { _, _ -> },
            onDismiss = {}
        )
    }
}
