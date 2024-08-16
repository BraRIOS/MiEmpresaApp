package com.brios.miempresa.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.R

@Composable
fun HomePage(products: List<Product>) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier
            .navigationBarsPadding()
            .statusBarsPadding(),
        topBar = {
            Text(text = stringResource(id = R.string.home_title),
                style = MaterialTheme.typography.displayLarge,)
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .padding(vertical = 4.dp)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            focusManager.clearFocus()
                        }
                    }
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ProductList(products = filteredProducts, onProductClick = {
                    focusManager.clearFocus()
                })
            }
        }
    )
}

@Composable
fun SearchBar(
    query: String, onQueryChange: (String) -> Unit,
    modifier: Modifier) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium),
        placeholder = {
            Text(stringResource(id = R.string.productSearch), color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Gray,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = Color.Gray,
            focusedPlaceholderColor = Color.Gray,
            unfocusedPlaceholderColor = Color.Gray,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductList(products: List<Product>, onProductClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            modifier = Modifier.padding(horizontal = 8.dp)
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
            contentDescription = stringResource(R.string.description)
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


@Composable
fun PreviewHomePage() {
    val sampleProducts = listOf(
        Product("Pepe CEO", "$10.00", "https://img4.s3wfg.com/web/img/images_uploaded/1/9/pepecoin-min.JPG"),
        Product("Pedro pedro pedro", "$20.00", "https://mixradio.co/wp-content/uploads/2024/05/pedro-mapache.jpg"),
        Product("Huh", "$30.00", "https://media.tenor.com/vmSP8owuOYYAAAAM/huh-cat-huh-m4rtin.gif"),
        Product("Shrek", "$40.00", "https://media.tenor.com/mtiOW6O-k8YAAAAM/shrek-shrek-rizz.gif"),
    )
    HomePage(products = sampleProducts, )
}

@Preview
@Composable
private fun HomePreview() {
    PreviewHomePage()
}

data class Product(
    val name: String,
    val price: String,
    val imageUrl: String
)
