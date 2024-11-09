package com.brios.miempresa.categories

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.components.DeleteDialog
import com.brios.miempresa.components.FABButton
import com.brios.miempresa.components.LoadingView
import com.brios.miempresa.components.ScaffoldedScreenComposable
import com.brios.miempresa.navigation.MiEmpresaScreen
import com.brios.miempresa.navigation.TopBarViewModel
import com.brios.miempresa.ui.dimens.AppDimensions
import com.brios.miempresa.ui.theme.PlaceholderBG

@Composable
fun CategoriesComposable(
    viewModel: TopBarViewModel = hiltViewModel(),
    categoriesViewModel: CategoriesViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val isLoading by categoriesViewModel.isLoading.collectAsState()
    val filteredCategories by categoriesViewModel.filteredCategories.collectAsState()
    categoriesViewModel.loadData()
    viewModel.topBarTitle = stringResource(id = R.string.categories_title)
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    ScaffoldedScreenComposable(
        navController = navController,
        floatingActionButton = {
            FABButton(
                action = { showAddDialog = true },
                actionText = stringResource(id = R.string.categories_action),
                actionIcon = Icons.Filled.Add
            )
        }
    ) {
        CategoriesScreenContent(
            navController,
            isLoading,
            {
                showEditDialog = true
                selectedCategory = it
            },
            {
                showDeleteDialog = true
                selectedCategory = it
            },
            filteredCategories)
    }
    if (showAddDialog){
        CategoryDialog(
            rowIndex = categoriesViewModel.getNextAvailableRowIndex(),
            onDismiss = { showAddDialog = false },
            onSave = { newCategory, onResult ->
                categoriesViewModel.addCategory(newCategory){ success ->
                    onResult(success)
                }
            }
        )
    } else if (selectedCategory != null){
        if (showEditDialog)
            CategoryDialog(
                category = selectedCategory,
                onDismiss = { showEditDialog = false },
                onSave = { newCategory, onResult ->
                    categoriesViewModel.updateCategory(newCategory){ success ->
                        onResult(success)
                    }
                }
            )
        else if (showDeleteDialog)
            DeleteDialog(
                itemName = selectedCategory!!.name,
                onDismiss = { showDeleteDialog = false },
                onConfirm = { categoriesViewModel.deleteCategory(selectedCategory!!){ success ->
                    if (success){
                        showDeleteDialog = false
                        selectedCategory = null
                    }
                } }
            )
    }
}

@Composable
private fun CategoriesScreenContent(
    navController: NavHostController,
    isLoading: Boolean,
    showEditDialog: (Category) -> Unit,
    showDeleteDialog: (Category) -> Unit,
    filteredCategories: List<Category>
) {
    if (isLoading)
        LoadingView()
    else
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(filteredCategories) { category ->
                CategoryListItem(category, showEditDialog, showDeleteDialog) {
                    navController.navigate(MiEmpresaScreen.Products.name)
                }
                HorizontalDivider()
            }
        }
}

@Composable
fun CategoryListItem(
    category: Category,
    showEditDialog: (Category) -> Unit,
    showDeleteDialog: (Category) -> Unit,
    onCategoryClick: (Category) -> Unit
) {
    var showDropdown by remember{
        mutableStateOf(false)
    }
    ListItem(
        headlineContent = { Text(category.name) },
        supportingContent = { Text(
            stringResource(
                R.string.products_label_count,
                category.productQty
            )) },
        leadingContent = {
            SubcomposeAsyncImage(
                model = category.imageUrl,
                loading = { CircularProgressIndicator() },
                contentDescription = null,
                modifier = Modifier
                    .size(AppDimensions.Categories.imageSize)
                    .clip(RectangleShape),
                contentScale = ContentScale.Crop,
                error = {
                    Column(
                        modifier = Modifier
                            .size(AppDimensions.Categories.imageSize)
                            .background(color = PlaceholderBG)
                            .padding(AppDimensions.smallPadding),
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier.height(AppDimensions.Categories.imageSize/2),
                            painter = painterResource(id = R.drawable.miempresa_logo_glyph),
                            contentDescription = stringResource(
                                R.string.placeholder
                            ),
                            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        )
                    }
                }
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = { showDropdown = true }) {
                    Icon(
                        Icons.Filled.MoreHoriz,
                        contentDescription =
                        stringResource(R.string.show_category_options, category.name)
                    )
                }
                DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false}) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = {
                            showEditDialog(category)
                            showDropdown = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.delete)) },
                        onClick = {
                            showDeleteDialog(category)
                            showDropdown = false
                        }
                    )
                }
            }
        },
        modifier = Modifier.clickable { onCategoryClick(category) }
    )
}

@Preview
@Composable
fun PreviewCategoryListItem(){
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    CategoriesScreenContent(
        navController = NavHostController(LocalContext.current),
        isLoading = false,
        filteredCategories = listOf(
            Category(
                rowIndex = 1,
                name = "Category 1",
                productQty = 10,
                imageUrl = "https://picsum.photos/200/300"
            ),
            Category(
                rowIndex = 2,
                name = "Category 2",
                productQty = 5,
                imageUrl = "https://picsum.photos/200/300"
            ),
            Category(
                rowIndex = 3,
                name = "Category 3",
                productQty = 8,
                imageUrl = "https://picsum.photos/200/300"
            )
        ),
        showEditDialog = {showDialog = true},
        showDeleteDialog = {showDeleteDialog = true}
    )
}

@Preview
@Composable
fun PreviewCategoryLoadingListItem(){
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    CategoriesScreenContent(
        navController = NavHostController(LocalContext.current),
        isLoading = true,
        filteredCategories = listOf(),
        showEditDialog = {showDialog = true},
        showDeleteDialog = {showDeleteDialog = true}
    )
}