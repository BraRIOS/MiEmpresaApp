package com.brios.miempresa.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.brios.miempresa.navigation.TopBar

@Composable
fun ProductDetails(
    navController: NavHostController,
    rowIndex: Int?,
    viewModel: ProductViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = rowIndex) {
        if (rowIndex != null) {
            viewModel.loadProduct(rowIndex)
        }
    }

    val productState by viewModel.currentProduct.collectAsState()
    val product = productState

    if (product != null) {
        ProductDetailsContent(product, navController, viewModel)
    } else {
        Text(stringResource(R.string.product_not_found))
    }
}

@Composable
private fun ProductDetailsContent(
    product: Product,
    navController: NavHostController,
    viewModel: ProductViewModel? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    Box(modifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()
        .statusBarsPadding()
        .background(MaterialTheme.colorScheme.secondaryContainer)
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
                    CircularProgressIndicator(modifier = Modifier.size(120.dp))
                } },
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        )
        TopBar(
            navController = navController,
            title = "",
            editProduct = { showDialog = true },
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
            SuggestionChip(
                onClick = { /*TODO*/ },
                label = { Text(product.category, style = MaterialTheme.typography.labelSmall) },
                colors = SuggestionChipDefaults.suggestionChipColors().copy(containerColor = MaterialTheme.colorScheme.primary, labelColor = Color.White),
                border = null,
                shape = CircleShape,
                modifier = Modifier
                )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.price,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(id = R.string.description_label), style = MaterialTheme.typography.labelLarge)
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
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.primary
                ),
            )
        }
        if (showDialog) {
            ProductDialog(
                product = product,
                onDismiss = { showDialog = false },
                onSave = { updatedProduct ->
                    viewModel?.updateProduct(updatedProduct)
                }
            )
        }
    }
}

@Preview
@Composable
fun ProductDetails(){
    ProductDetailsContent(
        product = Product(
            rowIndex = 1,
            name = "Preview",
            description = "Descripción de un producto para su vista previa",
            price = "$100",
            category = "Vista previa",
            imageUrl = "https://picsum.photos/200/300"
        ),
        navController = NavHostController(LocalContext.current)
    )
}