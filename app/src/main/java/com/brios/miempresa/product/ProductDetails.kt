package com.brios.miempresa.product

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.components.DeleteDialog
import com.brios.miempresa.components.MessageWithIcon
import com.brios.miempresa.navigation.TopBar

@Composable
fun ProductDetails(
    navController: NavHostController,
    rowIndex: Int?,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val loadingState by viewModel.isLoading.collectAsState()
    if (loadingState) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
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
        .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        if (product!=null) {
            SubcomposeAsyncImage(
                model = product.imageUrl,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .wrapContentSize(Alignment.Center)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(120.dp))
                    }
                },
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
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
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)

            ) {
                Spacer(modifier = Modifier.height(350.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = null,
                            shape = CircleShape,
                            modifier = Modifier
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.price,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.description_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = product.description,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    maxLines = 6,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors().copy(
                        unfocusedContainerColor = Color.White,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = Color.White
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