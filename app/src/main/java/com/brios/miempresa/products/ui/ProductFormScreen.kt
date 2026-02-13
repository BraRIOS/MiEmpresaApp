package com.brios.miempresa.products.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.brios.miempresa.R
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.core.ui.components.CategorySelectorBottomSheet
import com.brios.miempresa.core.ui.components.DeleteDialog
import com.brios.miempresa.core.ui.components.SimpleFormField
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray200

@Composable
fun ProductFormScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    viewModel: ProductFormViewModel = hiltViewModel(),
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val price by viewModel.price.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val isPublic by viewModel.isPublic.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val nameError by viewModel.nameError.collectAsStateWithLifecycle()
    val priceError by viewModel.priceError.collectAsStateWithLifecycle()
    val categoryError by viewModel.categoryError.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    val localImagePath by viewModel.localImagePath.collectAsStateWithLifecycle()
    val imageUrl = when {
        localImagePath != null -> localImagePath
        viewModel.isEditMode -> viewModel.originalImageUrl
        else -> null
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it.toString()) }
    }

    LaunchedEffect(Unit) {
        viewModel.saveComplete.collect { onNavigateBack() }
    }

    ProductFormContent(
        name = name,
        onNameChanged = viewModel::onNameChanged,
        price = price,
        onPriceChanged = viewModel::onPriceChanged,
        description = description,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        selectedCategoryId = selectedCategoryId,
        onCategorySelected = viewModel::onCategorySelected,
        isPublic = isPublic,
        onPublicChanged = viewModel::onPublicChanged,
        categories = categories,
        imageUrl = imageUrl,
        onImageClick = { imagePickerLauncher.launch("image/*") },
        onImageRemoved = viewModel::onImageRemoved,
        isEditMode = viewModel.isEditMode,
        isSaving = isSaving,
        onSave = viewModel::save,
        onCancel = viewModel::cancelSave,
        onNavigateBack = onNavigateBack,
        onNavigateToAddCategory = onNavigateToAddCategory,
        onDelete = viewModel::delete,
        nameError = nameError,
        priceError = priceError,
        categoryError = categoryError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormContent(
    name: String,
    onNameChanged: (String) -> Unit,
    price: String,
    onPriceChanged: (String) -> Unit,
    description: String,
    onDescriptionChanged: (String) -> Unit,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit,
    isPublic: Boolean,
    onPublicChanged: (Boolean) -> Unit,
    categories: List<Category>,
    imageUrl: String?,
    onImageClick: () -> Unit,
    onImageRemoved: () -> Unit,
    isEditMode: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onDelete: () -> Unit,
    nameError: String? = null,
    priceError: String? = null,
    categoryError: String? = null,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    BackHandler(enabled = isSaving) {
        // Prevent back navigation while saving
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (isEditMode) {
                                stringResource(R.string.edit_product)
                            } else {
                                stringResource(R.string.add_product)
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, enabled = !isSaving) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = Color.Unspecified,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.Unspecified,
                        actionIconContentColor = Color.Unspecified,
                    ),
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = SlateGray200,
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimensions.mediumLargePadding),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isSaving) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .weight(0.4f)
                                .height(56.dp),
                            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    } else if (isEditMode) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFFFEBEE), // red-50
                                contentColor = Color(0xFFE53935),   // red-600
                            ),
                            border = BorderStroke(
                                2.dp,
                                Color(0xFFFFCDD2) // red-100
                            ),
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                                modifier = Modifier.size(AppDimensions.defaultIconSize),
                            )
                        }
                    }

                    Button(
                        onClick = onSave,
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(if (isSaving) 0.5f else 1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(AppDimensions.smallIconSize),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                            Text(
                                text = stringResource(R.string.saving_product),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            Icon(
                                Icons.Filled.Save,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                            Text(
                                text = stringResource(R.string.save_product),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(AppDimensions.mediumPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.largePadding),
        ) {
            // Image picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(AppDimensions.mediumCornerRadius))
                    .then(
                        if (imageUrl != null) {
                            Modifier
                        } else {
                            val primaryColor = MaterialTheme.colorScheme.primary
                            Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                                .drawBehind {
                                    val stroke = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(10.dp.toPx(), 6.dp.toPx()),
                                            0f,
                                        ),
                                    )
                                    drawRoundRect(
                                        color = primaryColor.copy(alpha = 0.4f),
                                        style = stroke,
                                        cornerRadius = CornerRadius(AppDimensions.mediumCornerRadius.toPx()),
                                    )
                                }
                        },
                    )
                    .clickable(enabled = !isSaving) { onImageClick() },
                contentAlignment = Alignment.Center,
            ) {
                if (imageUrl != null) {
                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                    )

                    // Blurred background
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(radius = 20.dp)
                            .alpha(0.6f),
                    )

                    Image(
                        painter = painter,
                        contentDescription = stringResource(R.string.photo_label),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .fillMaxHeight()
                            .padding(AppDimensions.smallPadding),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Remove button
                        if (!isSaving) {
                            SmallFloatingActionButton(
                                modifier = Modifier
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                    .size(AppDimensions.smallFabSize),
                                onClick = onImageRemoved,
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.remove_photo),
                                    modifier = Modifier.size(AppDimensions.smallFabIconSize),
                                )
                            }
                            // Edit button
                            SmallFloatingActionButton(
                                modifier = Modifier
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                    .size(AppDimensions.smallFabSize),
                                onClick = { onImageClick() },
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.edit_photo),
                                    modifier = Modifier.size(AppDimensions.smallFabIconSize),
                                )
                            }
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.AddAPhoto,
                            contentDescription = null,
                            tint = if (isSaving) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.mediumIconSize),
                        )
                        Text(
                            text = stringResource(R.string.photo_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSaving) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            // Card 1: Name + Price
            Card(
                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Name field
                    SimpleFormField(
                        label = stringResource(R.string.product_name_label),
                        value = name,
                        onValueChange = onNameChanged,
                        placeholder = stringResource(R.string.product_name_placeholder),
                        isError = nameError != null,
                        errorText = nameError,
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = AppDimensions.mediumPadding),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Price field
                    SimpleFormField(
                        label = stringResource(R.string.price_label),
                        value = price,
                        onValueChange = onPriceChanged,
                        placeholder = "0.00",
                        prefix = "$",
                        isError = priceError != null,
                        errorText = priceError,
                        keyboardType = KeyboardType.Decimal,
                    )
                }
            }

            // Card 2: Description + Category
            Card(
                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Description
                    SimpleFormField(
                        label = stringResource(R.string.description_label),
                        value = description,
                        onValueChange = onDescriptionChanged,
                        placeholder = stringResource(R.string.description_placeholder),
                        singleLine = false,
                        minLines = 3,
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = AppDimensions.mediumPadding),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Category dropdown
                    Column(
                        modifier =
                            Modifier.padding(
                                horizontal = AppDimensions.mediumPadding,
                                vertical = AppDimensions.mediumPadding,
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.category_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                        if (categories.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_categories_create_first),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = AppDimensions.smallPadding),
                            )
                            TextButton(onClick = onNavigateToAddCategory) {
                                Text(stringResource(R.string.create_category))
                            }
                        } else {
                            Surface(
                                onClick = { showCategorySheet = true },
                                color = Color.Transparent,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = AppDimensions.smallPadding),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = categories.find { it.id == selectedCategoryId }?.let {
                                            if (it.iconEmoji.isNotEmpty()) "${it.iconEmoji} ${it.name}" else it.name
                                        } ?: stringResource(R.string.select_category_placeholder),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (selectedCategoryId != null) {
                                            MaterialTheme.colorScheme.onBackground
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                .copy(alpha = 0.5f)
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            if (categoryError != null) {
                                Text(
                                    text = categoryError,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = AppDimensions.extraSmallPadding),
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: Public visibility switch
            Card(
                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(AppDimensions.mediumPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.visible_public_catalog),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = stringResource(R.string.public_switch_helper),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isPublic,
                        onCheckedChange = onPublicChanged,
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteDialog(
            itemName = name,
            title = stringResource(R.string.delete_product),
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
        )
    }

    if (showCategorySheet) {
        CategorySelectorBottomSheet(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = { id ->
                if (id != null) onCategorySelected(id)
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false },
            onCreateCategory = onNavigateToAddCategory,
        )
    }
}

private val sampleCategories =
    listOf(
        Category(id = "1", name = "Café", iconEmoji = "☕", companyId = "c1"),
        Category(id = "2", name = "Panadería", iconEmoji = "🍞", companyId = "c1"),
        Category(id = "3", name = "Postres", iconEmoji = "🍰", companyId = "c1"),
        Category(id = "4", name = "Bebidas", iconEmoji = "🥤", companyId = "c1"),
    )

@Preview(showBackground = true)
@Composable
private fun ProductFormPreview() {
    MiEmpresaTheme {
        ProductFormContent(
            name = "Café Latte",
            onNameChanged = {},
            price = "4.50",
            onPriceChanged = {},
            description = "Un delicioso café con leche",
            onDescriptionChanged = {},
            selectedCategoryId = "1",
            onCategorySelected = {},
            isPublic = true,
            onPublicChanged = {},
            categories = sampleCategories,
            imageUrl = "",
            onImageClick = {},
            onImageRemoved = {},
            isEditMode = true,
            isSaving = false,
            onSave = {},
            onCancel = {},
            onNavigateBack = {},
            onNavigateToAddCategory = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductFormNewPreview() {
    MiEmpresaTheme {
        ProductFormContent(
            name = "",
            onNameChanged = {},
            price = "",
            onPriceChanged = {},
            description = "",
            onDescriptionChanged = {},
            selectedCategoryId = null,
            onCategorySelected = {},
            isPublic = true,
            onPublicChanged = {},
            categories = sampleCategories,
            imageUrl = null,
            onImageClick = {},
            onImageRemoved = {},
            isEditMode = false,
            isSaving = false,
            onSave = {},
            onCancel = {},
            onNavigateBack = {},
            onNavigateToAddCategory = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductFormSavingPreview() {
    MiEmpresaTheme {
        ProductFormContent(
            name = "Café Latte",
            onNameChanged = {},
            price = "4.50",
            onPriceChanged = {},
            description = "Un delicioso café con leche",
            onDescriptionChanged = {},
            selectedCategoryId = "1",
            onCategorySelected = {},
            isPublic = true,
            onPublicChanged = {},
            categories = sampleCategories,
            imageUrl = null,
            onImageClick = {},
            onImageRemoved = {},
            isEditMode = true,
            isSaving = true,
            onSave = {},
            onCancel = {},
            onNavigateBack = {},
            onNavigateToAddCategory = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductFormErrorPreview() {
    MiEmpresaTheme {
        ProductFormContent(
            name = "",
            onNameChanged = {},
            price = "invalid",
            onPriceChanged = {},
            description = "",
            onDescriptionChanged = {},
            selectedCategoryId = null,
            onCategorySelected = {},
            isPublic = true,
            onPublicChanged = {},
            categories = sampleCategories,
            imageUrl = null,
            onImageClick = {},
            onImageRemoved = {},
            isEditMode = false,
            isSaving = false,
            onSave = {},
            onCancel = {},
            onNavigateBack = {},
            onNavigateToAddCategory = {},
            onDelete = {},
            nameError = "El nombre es requerido",
            priceError = "Precio inválido",
            categoryError = "Debes seleccionar una categoría"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductFormNoCategoriesPreview() {
    MiEmpresaTheme {
        ProductFormContent(
            name = "",
            onNameChanged = {},
            price = "",
            onPriceChanged = {},
            description = "",
            onDescriptionChanged = {},
            selectedCategoryId = null,
            onCategorySelected = {},
            isPublic = true,
            onPublicChanged = {},
            categories = emptyList(),
            imageUrl = null,
            onImageClick = {},
            onImageRemoved = {},
            isEditMode = false,
            isSaving = false,
            onSave = {},
            onCancel = {},
            onNavigateBack = {},
            onNavigateToAddCategory = {},
            onDelete = {},
        )
    }
}
