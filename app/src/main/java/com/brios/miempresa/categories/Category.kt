package com.brios.miempresa.categories

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun Categories(
    viewModel: MainViewModel,
    onCategorySelect: () -> Unit = {}
) {
    val products = listOf(
        Category("Meme",
            4,
            "https://img4.s3wfg.com/web/img/images_uploaded/1/9/pepecoin-min.JPG"),
    )
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header(
            title = stringResource(id = R.string.categories_title),
            hasAction = true,
            action = { /* Acción al presionar el botón */ },
            actionText = stringResource(id = R.string.categories_action),
            actionIcon = Icons.Filled.Add,
            hasSearch = true,
            searchPlaceholder = stringResource(id = R.string.categorySearch),
            searchQuery = searchQuery,
            onQueryChange = { searchQuery = it }
        )
        CategoryGrid(categories = filteredProducts, onProductClick = {
            focusManager.clearFocus()
        })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryGrid(categories: List<Category>, onProductClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            categories.forEach { product ->
                CategoryCard(product, onProductClick)
            }
        }
    }
}


@Composable
fun CategoryCard(category: Category, onProductClick: () -> Unit) {
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
            model = category.imageUrl,
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
            contentDescription = category.name + " image",
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = category.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "${stringResource(id = R.string.home_title)}: ${category.productQty}",
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

data class Category(
    val name: String,
    val productQty: Int,
    val imageUrl: String
)