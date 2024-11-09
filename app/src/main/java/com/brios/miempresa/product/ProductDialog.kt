package com.brios.miempresa.product

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.R
import com.brios.miempresa.categories.Category
import com.brios.miempresa.components.SearchableDropdownWithChips
import com.brios.miempresa.ui.dimens.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    rowIndex: Int? = null,
    product: Product? = null,
    categories: List<Category>,
    onDismiss: () ->Unit,
    onSave: (Product, List<Category>, (Boolean) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.removePrefix("$") ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var showError by remember { mutableStateOf(false) }
    val selectedCategoriesNames = remember { mutableStateListOf<String>() }
    val context = LocalContext.current

    LaunchedEffect(key1 = product, key2 = categories) {
        if (product != null && selectedCategoriesNames.isEmpty()) {
            selectedCategoriesNames.addAll(product.categories)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Top
    ){
        TopAppBar(
            navigationIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.go_back),
                    modifier = Modifier
                        .padding(start = AppDimensions.smallPadding, end = AppDimensions.mediumPadding)
                        .size(AppDimensions.mediumIconSize)
                        .clickable {
                            onDismiss()
                        }
                )
            },
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                    Text(
                        if (product == null)
                            stringResource(R.string.add_product) else stringResource(R.string.edit_product),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = stringResource(R.string.save),
                    modifier = Modifier
                        .padding(start = AppDimensions.smallPadding, end = AppDimensions.mediumPadding)
                        .size(AppDimensions.mediumIconSize)
                        .clickable {
                            if (name.isNotBlank() && selectedCategoriesNames.isNotEmpty()) {
                                val updatedProduct = Product(
                                    rowIndex ?: product!!.rowIndex,
                                    name,
                                    description,
                                    price,
                                    selectedCategoriesNames,
                                    imageUrl
                                )
                                val selectedCategories = categories.filter { updatedProduct.categories.contains(it.name) }
                                onSave(updatedProduct, selectedCategories) { success ->
                                    if (success) {
                                        onDismiss()
                                    }
                                }
                            } else {
                                showError = true
                            }
                        },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
        LazyColumn(
            modifier = Modifier
                .clip(RoundedCornerShape(AppDimensions.mediumPadding))
                .padding(AppDimensions.mediumPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding)
        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_label)) },
                    isError = name.isBlank() && showError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description_label)) },
                    maxLines = 4
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text(text = "$")},
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(stringResource(R.string.price_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
            item {
                SearchableDropdownWithChips(
                    label = stringResource(R.string.categories_search),
                    items = categories.map { it.name },
                    isError= selectedCategoriesNames.isEmpty() && showError,
                    selectedItems = selectedCategoriesNames,
                    onItemSelected = { selectedCategoriesNames.add(it) },
                    onItemRemoved = { selectedCategoriesNames.remove(it) }
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text(stringResource(R.string.image_url_label)) },
                    singleLine = true
                )
            }
            if (showError) {
                Toast.makeText(context, R.string.product_dialog_required_fields, Toast.LENGTH_SHORT).show()
                item {
                    Text(
                        text = stringResource(R.string.product_dialog_required_fields),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun NewProductDialogPreview(){
    ProductDialog(
        categories = listOf(
            Category(1, "Category 1",4,""),
            Category(2, "Category 2", 10, "")
        ),
        onDismiss = {},
        onSave = { _, _, _-> }
    )
}

@Preview
@Composable
fun UpdateProductDialogPreview(){
    ProductDialog(
        product = Product(
            rowIndex = 1,
            name = "Product",
            description = "Description DescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescription",
            price = "10,00",
            categories = listOf("Category 1", "Category 2"),
            imageUrl = "https://picsum.photos/200/300"
        ),
        categories = listOf(
            Category(1, "Category 1",4,""),
            Category(2, "Category 2", 10, "")
        ),
        onDismiss = {},
        onSave = { _, _, _ -> }
    )
}