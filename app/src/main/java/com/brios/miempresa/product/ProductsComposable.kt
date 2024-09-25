package com.brios.miempresa.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.common.FABButton
import com.brios.miempresa.common.Header
import com.brios.miempresa.common.ScaffoldedScreenComposable
import com.brios.miempresa.navigation.MiEmpresaScreen
import com.brios.miempresa.navigation.TopBarViewModel

@Composable
fun ProductsComposable(
    topBarViewModel: TopBarViewModel = hiltViewModel(),
    productsViewModel: ProductsViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val searchQuery by productsViewModel.searchQuery.collectAsState()
    val isLoading by productsViewModel.isLoading.collectAsState()
    val filteredProducts by productsViewModel.filteredProducts.collectAsState()

    val focusManager = LocalFocusManager.current
    val windowTitle = stringResource(id = R.string.home_title)
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { firstVisibleItemIndex ->
                topBarViewModel.topBarTitle = if (firstVisibleItemIndex > 0) {
                    windowTitle
                } else {
                    ""
                }
            }
    }
    var showDialog by remember { mutableStateOf(false) }

    productsViewModel.loadData()
    ScaffoldedScreenComposable(
        navController = navController,
        floatingActionButton = {
            FABButton(
                action = { showDialog = true },
                actionText = stringResource(id = R.string.add_product),
                actionIcon = Icons.Filled.Add
            )
        }
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                },
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Header(
                    title = windowTitle,
                    hasSearch = true,
                    searchPlaceholder = stringResource(id = R.string.productSearch),
                    searchQuery = searchQuery,
                    onQueryChange = { productsViewModel.onSearchQueryChange(it) }
                )
            }

            if (isLoading) {
                item {
                    CircularProgressIndicator()
                }
            } else {
                items(filteredProducts.chunked(10)) { rowItems ->
                    ProductGrid(rowItems) { selectedProduct ->
                        navController.navigate(MiEmpresaScreen.Product.name + "/${selectedProduct.rowIndex}")
                        focusManager.clearFocus()
                    }
                }
            }
        }
    }
    if (showDialog) {
        ProductDialog(
            rowIndex = productsViewModel.getNextAvailableRowIndex(),
            onDismiss = { showDialog = false },
            onSave = { newProduct ->
                productsViewModel.addProduct(newProduct)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductGrid(products: List<Product>, onProductClick: (product:Product) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            products.forEach { product ->
                ProductCard(product, onProductClick)
            }
        }
    }
}


@Composable
fun ProductCard(product: Product, onProductClick: (product:Product) -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .width(150.dp)
            .clickable(
                onClick = { onProductClick(product) },
            ),
    ) {
        SubcomposeAsyncImage(
            model = product.imageUrl,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(54.dp))
                }
            },
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(),
            contentDescription = product.name + " image",
            )
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(text = product.name, style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Ellipsis, maxLines = 2, minLines = 2)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(text = product.price, style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Preview
@Composable
fun PreviewProductCard() {
    ProductCard(
        product =
        Product(
            rowIndex = 1,
            name = "Preview",
            description = "",
            price = "$100",
            category = "",
            imageUrl = "https://picsum.photos/200/300"
        )
    ){}
}