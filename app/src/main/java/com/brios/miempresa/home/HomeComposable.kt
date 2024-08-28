package com.brios.miempresa.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.common.Header
import com.brios.miempresa.model.MainViewModel

@Composable
fun HomeComposable(
    viewModel: MainViewModel,
    onNavigateToProductDetail: () -> Unit = {}
) {
    val products = listOf(
        Product(
            "Pepe CEO",
            "Meme de Pepe el sapo que se convirtió en CEO exitoso",
            "$10.00",
            "Meme",
            "https://img4.s3wfg.com/web/img/images_uploaded/1/9/pepecoin-min.JPG"
        ),
        Product(
            "Pedro pedro pedro",
            "Meme del mapache con la canción de pedro pedro pedro",
            "$20.00",
            "Meme",
            "https://mixradio.co/wp-content/uploads/2024/05/pedro-mapache.jpg"
        ),
        Product(
            "Huh",
            "Meme de gato huh",
            "$30.00",
            "Meme",
            "https://media.tenor.com/vmSP8owuOYYAAAAM/huh-cat-huh-m4rtin.gif"
        ),
        Product(
            "Shrek",
            "Meme de Shrek sospechoso",
            "$40.00",
            "Meme",
            "https://media.tenor.com/mtiOW6O-k8YAAAAM/shrek-shrek-rizz.gif"
        ),
        Product(
            "Shrek",
            "Meme de Shrek sospechoso",
            "$40.00",
            "Meme",
            "https://media.tenor.com/mtiOW6O-k8YAAAAM/shrek-shrek-rizz.gif"
        ),
        Product(
            "Shrek",
            "Meme de Shrek sospechoso",
            "$40.00",
            "Meme",
            "https://media.tenor.com/mtiOW6O-k8YAAAAM/shrek-shrek-rizz.gif"
        ),
        Product(
            "Shrek",
            "Meme de Shrek sospechoso",
            "$40.00",
            "Meme",
            "https://media.tenor.com/mtiOW6O-k8YAAAAM/shrek-shrek-rizz.gif"
        ),
    )
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val focusManager = LocalFocusManager.current
    val windowTitle = stringResource(id = R.string.home_title)
    val lazyListState = rememberLazyListState()

    // Detectar si el Header está visible
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { firstVisibleItemIndex ->
                viewModel.topBarTitle = if (firstVisibleItemIndex > 0) {
                    windowTitle
                } else {
                    ""
                }
            }
    }

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
                hasAction = true,
                action = { /* Acción al presionar el botón */ },
                actionText = stringResource(id = R.string.home_action),
                actionIcon = Icons.Filled.Add,
                hasSearch = true,
                searchPlaceholder = stringResource(id = R.string.productSearch),
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it }
            )
        }

        items(filteredProducts.chunked(10)) { rowItems ->
            ProductGrid(rowItems) {
                onNavigateToProductDetail()
                focusManager.clearFocus()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductGrid(products: List<Product>, onProductClick: () -> Unit) {
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
fun ProductCard(product: Product, onProductClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(
                onClick = onProductClick,
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        )
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
                .height(150.dp)
                .fillMaxWidth(),
            contentDescription = product.name + " image",
            )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = product.name, style = MaterialTheme.typography.titleLarge)
            Text(text = product.price, style = MaterialTheme.typography.titleLarge)
        }
    }
}

data class Product(
    val name: String,
    val description: String,
    val price: String,
    val category: String,
    val imageUrl: String
)
