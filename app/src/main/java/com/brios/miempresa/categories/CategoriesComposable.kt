package com.brios.miempresa.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.common.Header
import com.brios.miempresa.navigation.TopBarViewModel

@Composable
fun CategoriesComposable(
    viewModel: TopBarViewModel,
    categoriesViewModel: CategoriesViewModel = hiltViewModel(),
    onCategorySelect: () -> Unit = {}
) {
    val searchQuery by categoriesViewModel.searchQuery.collectAsState()
    val isLoading by categoriesViewModel.isLoading.collectAsState()
    val filteredCategories by categoriesViewModel.filteredCategories.collectAsState()

    val focusManager = LocalFocusManager.current
    val windowTitle = stringResource(id = R.string.categories_title)
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
                actionText = stringResource(id = R.string.categories_action),
                actionIcon = Icons.Filled.Add,
                hasSearch = true,
                searchPlaceholder = stringResource(id = R.string.categorySearch),
                searchQuery = searchQuery,
                onQueryChange = { categoriesViewModel.onSearchQueryChange(it) }
            )
        }

        if (isLoading) {
            item {
                CircularProgressIndicator()
            }
        } else {
            items(filteredCategories) { rowItems ->
                CategoryCard(rowItems, onCategorySelect)
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, onCategorySelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onCategorySelect,
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