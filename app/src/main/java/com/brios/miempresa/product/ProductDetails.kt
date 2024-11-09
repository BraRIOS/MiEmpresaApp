package com.brios.miempresa.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.components.DeleteDialog
import com.brios.miempresa.components.LoadingView
import com.brios.miempresa.components.MessageWithIcon
import com.brios.miempresa.navigation.TopBar
import com.brios.miempresa.ui.dimens.AppDimensions
import com.brios.miempresa.ui.theme.OnPlaceholderBG
import com.brios.miempresa.ui.theme.PlaceholderBG

@Composable
fun ProductDetails(
    navController: NavHostController,
    rowIndex: Int?,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val loadingState by viewModel.isLoading.collectAsState()
    if (loadingState) {
        LoadingView()
    }

    LaunchedEffect(key1 = rowIndex) {
        if (rowIndex != null) {
            viewModel.loadProduct(rowIndex)
        }
    }

    val productState by viewModel.currentProduct.collectAsState()
    val product = productState

    ProductDetailsContent(product, loadingState, navController, viewModel)

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetailsContent(
    product: Product? = null,
    loadingState: Boolean,
    navController: NavHostController,
    viewModel: ProductViewModel? = null
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()
        .statusBarsPadding()
        .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        if (product!=null) {
            SubcomposeAsyncImage(
                model = product.imageUrl,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AppDimensions.mediumPadding)
                            .wrapContentSize(Alignment.Center)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(AppDimensions.ProductDetails.progressIndicatorSize))
                    }
                },
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.ProductDetails.productImageSize),
                error = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = PlaceholderBG)
                            .padding(AppDimensions.smallPadding),
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier.height(AppDimensions.ProductDetails.productImageSize/2),
                            painter = painterResource(id = R.drawable.miempresa_logo_glyph),
                            contentDescription = stringResource(
                                R.string.placeholder
                            ),
                            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        )
                        Text(
                            text = stringResource(R.string.sin_imagen),
                            style = MaterialTheme.typography.displayMedium,
                            color = OnPlaceholderBG
                        )
                    }
                }
            )
            TopBar(
                navController = navController,
                title = "",
                editProduct = { showEditDialog = true },
                deleteProduct = { showDeleteDialog = true }
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = AppDimensions.mediumPadding,
                        end = AppDimensions.mediumPadding,
                        top = AppDimensions.smallPadding
                    )

            ) {
                Spacer(modifier = Modifier.height(AppDimensions.ProductDetails.productImageSize))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding)
                ) {
                    product.categories.map { category ->
                        SuggestionChip(
                            onClick = { /*TODO ir a search de productos con filtro*/ },
                            label = {
                                Text(
                                    category,
                                    style = MaterialTheme.typography.labelSmall
                                )},
                            colors = SuggestionChipDefaults.suggestionChipColors().copy(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                labelColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            border = null,
                            shape = CircleShape,
                            modifier = Modifier
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
                Text(
                    text = product.price,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
                Text(
                    text = stringResource(id = R.string.description_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))
                OutlinedTextField(
                    value = product.description,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    maxLines = 6,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    colors = TextFieldDefaults.colors().copy(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                )
            }
            if (showEditDialog && viewModel != null) {
                viewModel.loadCategories()
                val categories by viewModel.categories.collectAsState()
                ProductDialog(
                    product = product,
                    categories = categories,
                    onDismiss = { showEditDialog = false },
                    onSave = { updatedProduct, selectedCategories, onResult ->
                        viewModel.updateProduct(updatedProduct, selectedCategories) { success ->
                            onResult(success)
                        }
                    }
                )
            }
            if (showDeleteDialog){
                    DeleteDialog(
                        itemName = product.name,
                        onDismiss = { showDeleteDialog = false },
                        onConfirm = {
                            viewModel!!.deleteProduct(product.rowIndex) { success ->
                                if (success) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    )
            }
        }else if (!loadingState){
            TopBar(
                navController = navController,
                title = "",
                editProduct = { showEditDialog = true },
                
            )
            MessageWithIcon(stringResource(R.string.product_not_found), Icons.Filled.Warning)
        }
    }
}

@Preview
@Composable
fun ProductDetailsWithProduct(){
    ProductDetailsContent(
        product = Product(
            rowIndex = 1,
            name = "Preview",
            description = "Descripción de un producto para su vista previa",
            price = "$100",
            categories = listOf("Vista previa", "Elemento de prueba",
                "Probando categoría larga", "Probando"),
            imageUrl = "https://picsum.photos/200/300"
        ),
        loadingState = false,
        navController = NavHostController(LocalContext.current)
    )
}

@Preview
@Composable
fun ProductDetailsWithoutProduct(){
    ProductDetailsContent(
        loadingState = false,
        navController = NavHostController(LocalContext.current)
    )
}