package com.brios.miempresa.product

import ProductDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.navigation.TopBar

@Composable
fun ProductDetails(
    navController: NavHostController,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val product = navController.previousBackStackEntry?.savedStateHandle?.get<Product>("product")
    var showDialog by remember { mutableStateOf(false) }

    if (product != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = product.imageUrl,
                loading = { CircularProgressIndicator() },
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TopBar(
                    navController = navController,
                    title = "",
                    editProduct = { showDialog = true }
                )
                Spacer(modifier = Modifier.height(250.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.price,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                SuggestionChip(onClick = { /*TODO*/ }, label = { Text(product.category) })
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = product.description, style = MaterialTheme.typography.bodyMedium)
            }
            if (showDialog) {
                ProductDialog(
                    product = product,
                    onDismiss = { showDialog = false },
                    onSave = { updatedProduct ->
                        viewModel.updateProduct(updatedProduct)
                    }
                )
            }
        }
    } else {
        // Handle case where product is null
        Text("Product not found")
    }
}