package com.brios.miempresa.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.categories.Category
import com.brios.miempresa.components.FABButton
import com.brios.miempresa.components.LoadingView
import com.brios.miempresa.components.ScaffoldedScreenComposable
import com.brios.miempresa.components.SearchBar
import com.brios.miempresa.navigation.MiEmpresaScreen
import com.brios.miempresa.navigation.TopBarViewModel
import com.brios.miempresa.ui.dimens.AppDimensions
import com.brios.miempresa.ui.theme.MiEmpresaTheme
import com.brios.miempresa.ui.theme.OnPlaceholderBG
import com.brios.miempresa.ui.theme.PlaceholderBG

@Composable
fun ProductsComposable(
    topBarViewModel: TopBarViewModel = hiltViewModel(),
    productsViewModel: ProductsViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val isLoading by productsViewModel.isLoading.collectAsState()
    val filteredProducts by productsViewModel.filteredProducts.collectAsState()
    val categories by productsViewModel.categories.collectAsState()

    topBarViewModel.topBarTitle = stringResource(id = R.string.home_title)

    LaunchedEffect(Unit) {
        productsViewModel.loadData()
    }

    ProductsScreen(
        navController = navController,
        isLoading = isLoading,
        filteredProducts = filteredProducts,
        categories = categories,
        onSearchQueryChange = productsViewModel::onSearchQueryChange,
        onProductClick = { selectedProduct ->
            navController.navigate(MiEmpresaScreen.Product.name + "/${selectedProduct.rowIndex}")
        },
        onLoadCategories = productsViewModel::loadCategories,
        getNextAvailableRowIndex = productsViewModel::getNextAvailableRowIndex,
        onAddProduct = { newProduct, selectedCategories, onResult ->
            productsViewModel.addProduct(newProduct, selectedCategories, onResult)
        },
    )
}

@Composable
fun ProductsScreen(
    navController: NavHostController,
    isLoading: Boolean,
    filteredProducts: List<Product>,
    categories: List<Category>,
    onSearchQueryChange: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onLoadCategories: () -> Unit,
    getNextAvailableRowIndex: () -> Int,
    onAddProduct: (Product, List<Category>, (Boolean) -> Unit) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        onLoadCategories()
        ProductDialog(
            rowIndex = getNextAvailableRowIndex(),
            categories = categories,
            onDismiss = { showDialog = false },
            onSave = { newProduct, selectedCategories, onResult ->
                onAddProduct(newProduct, selectedCategories) { success ->
                    onResult(success)
                }
            },
        )
    } else {
        ScaffoldedScreenComposable(
            navController = navController,
            floatingActionButton = {
                FABButton(
                    action = { showDialog = true },
                    actionText = stringResource(id = R.string.add_product),
                    actionIcon = Icons.Filled.Add,
                )
            },
        ) {
            if (isLoading) {
                LoadingView()
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    focusManager.clearFocus()
                                }
                            },
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                onSearchQueryChange(it)
                            },
                            modifier =
                                Modifier
                                    .padding(vertical = AppDimensions.smallPadding),
                            placeholderText = stringResource(id = R.string.productSearch),
                        )
                    }

                    items(filteredProducts.chunked(10)) { rowItems ->
                        ProductGrid(rowItems) { selectedProduct ->
                            onProductClick(selectedProduct)
                            focusManager.clearFocus()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductGrid(
    products: List<Product>,
    onProductClick: (product: Product) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = AppDimensions.mediumPadding),
        contentAlignment = Alignment.Center,
    ) {
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding, Alignment.Top),
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding, Alignment.CenterHorizontally),
        ) {
            products.forEach { product ->
                ProductCard(product, onProductClick)
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onProductClick: (product: Product) -> Unit,
) {
    ElevatedCard(
        modifier =
            Modifier
                .width(AppDimensions.Products.productCardWidth)
                .clickable(
                    onClick = { onProductClick(product) },
                ),
    ) {
        SubcomposeAsyncImage(
            model = product.imageUrl,
            loading = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(AppDimensions.mediumPadding)
                            .wrapContentSize(Alignment.Center),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(AppDimensions.Products.progressIndicatorSize))
                }
            },
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .height(AppDimensions.Products.imageHeight)
                    .fillMaxWidth(),
            contentDescription = stringResource(R.string.image, product.name),
            error = {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(color = PlaceholderBG)
                            .padding(AppDimensions.smallPadding),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            AppDimensions.extraSmallPadding,
                            Alignment.CenterVertically,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        modifier = Modifier.height(AppDimensions.Products.imageHeight / 2),
                        painter = painterResource(id = R.drawable.miempresa_logo_glyph),
                        contentDescription =
                            stringResource(
                                R.string.placeholder,
                            ),
                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                    )
                    Text(
                        text = stringResource(R.string.sin_imagen),
                        style = MaterialTheme.typography.labelMedium,
                        color = OnPlaceholderBG,
                    )
                }
            },
        )
        Column(
            modifier =
                Modifier
                    .padding(AppDimensions.smallPadding)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                minLines = 2,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = product.price,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis,
                )
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
                categories = listOf(),
                imageUrl = "https://picsum.photos/200/300",
            ),
    ) {}
}

@Preview
@Composable
fun PreviewProductGrid() {
    ProductGrid(
        listOf(
            Product(
                rowIndex = 1,
                name = "Preview",
                description = "",
                price = "$100",
                categories = listOf(),
                imageUrl = "https://picsum.photos/200/300",
            ),
            Product(
                rowIndex = 1,
                name = "Preview",
                description = "",
                price = "$100",
                categories = listOf(),
                imageUrl = "https://picsum.photos/200/300",
            ),
            Product(
                rowIndex = 1,
                name = "Preview",
                description = "",
                price = "$100",
                categories = listOf(),
                imageUrl = "https://picsum.photos/200/300",
            ),
        ),
    ) {}
}

@Preview
@Composable
fun PreviewProductsScreen() {
    val sampleProducts =
        listOf(
            Product(
                rowIndex = 1,
                name = "Product 1",
                description = "Description 1",
                price = "$100",
                categories = listOf("Category 1"),
                imageUrl = "https://picsum.photos/200/300",
            ),
            Product(
                rowIndex = 2,
                name = "Product 2",
                description = "Description 2",
                price = "$200",
                categories = listOf("Category 2"),
                imageUrl = "https://picsum.photos/200/301",
            ),
        )

    val sampleCategories =
        listOf(
            Category(1, "Category 1", 5, ""),
            Category(2, "Category 2", 10, ""),
        )

    MiEmpresaTheme {
        ProductsScreen(
            navController = rememberNavController(),
            isLoading = false,
            filteredProducts = sampleProducts,
            categories = sampleCategories,
            onSearchQueryChange = {},
            onProductClick = {},
            onLoadCategories = {},
            getNextAvailableRowIndex = { 3 },
            onAddProduct = { _, _, result -> result(true) },
        )
    }
}
