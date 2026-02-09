package com.brios.miempresa.products.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
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
    val saveComplete by viewModel.saveComplete.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(saveComplete) {
        if (saveComplete) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.isEditMode) {
                            stringResource(R.string.edit_product)
                        } else {
                            stringResource(R.string.add_product)
                        },
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (viewModel.isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
            ) {
                Button(
                    onClick = viewModel::save,
                    enabled = !isSaving,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(AppDimensions.mediumLargePadding)
                            .height(56.dp),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(
                        Icons.Outlined.Save,
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
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                OutlinedButton(
                    onClick = { /* TODO: ACTION_PICK */ },
                    modifier = Modifier.size(128.dp),
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    border =
                        BorderStroke(
                            AppDimensions.mediumBorderWidth,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        ),
                    colors =
                        androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Outlined.AddAPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.mediumIconSize),
                        )
                        Text(
                            text = stringResource(R.string.photo_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            // Card 1: Name + Price
            Card(
                shape = RoundedCornerShape(AppDimensions.inputCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Name field
                    FormField(
                        label = stringResource(R.string.product_name_label),
                        value = name,
                        onValueChange = viewModel::onNameChanged,
                        placeholder = stringResource(R.string.product_name_placeholder),
                        isError = nameError != null,
                        errorText = nameError,
                        showDivider = true,
                    )
                    // Price field
                    FormField(
                        label = stringResource(R.string.price_label),
                        value = price,
                        onValueChange = viewModel::onPriceChanged,
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
                shape = RoundedCornerShape(AppDimensions.inputCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Description
                    FormField(
                        label = stringResource(R.string.description_label),
                        value = description,
                        onValueChange = viewModel::onDescriptionChanged,
                        placeholder = stringResource(R.string.description_placeholder),
                        singleLine = false,
                        minLines = 3,
                        showDivider = true,
                    )
                    // Category dropdown
                    Column(
                        modifier =
                            Modifier.padding(
                                horizontal = AppDimensions.mediumPadding,
                                vertical = AppDimensions.mediumSmallPadding,
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.category_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = it },
                            ) {
                                TextField(
                                    value =
                                        categories.find { it.id == selectedCategoryId }?.let {
                                            "${it.iconEmoji} ${it.name}"
                                        } ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = {
                                        Text(
                                            stringResource(R.string.select_category_placeholder),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        )
                                    },
                                    isError = categoryError != null,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                    },
                                    colors =
                                        TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                        ),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false },
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text("${category.iconEmoji} ${category.name}") },
                                            onClick = {
                                                viewModel.onCategorySelected(category.id)
                                                categoryExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                            if (categoryError != null) {
                                Text(
                                    text = categoryError!!,
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
                shape = RoundedCornerShape(AppDimensions.inputCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        )
                        Text(
                            text = stringResource(R.string.public_switch_helper),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isPublic,
                        onCheckedChange = viewModel::onPublicChanged,
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_product)) },
            text = { Text(stringResource(R.string.confirm_delete_product)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    showDivider: Boolean = false,
) {
    Column(
        modifier =
            modifier.padding(
                start = AppDimensions.mediumPadding,
                end = AppDimensions.mediumPadding,
                top = AppDimensions.mediumPadding,
                bottom = if (showDivider) AppDimensions.extraSmallPadding else AppDimensions.mediumPadding,
            ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color =
                if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (prefix != null) {
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = AppDimensions.extraSmallPadding),
                )
            }
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                },
                singleLine = singleLine,
                minLines = minLines,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor =
                            if (showDivider) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                        unfocusedIndicatorColor =
                            if (showDivider) {
                                MaterialTheme.colorScheme.outlineVariant
                            } else {
                                Color.Transparent
                            },
                    ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = AppDimensions.extraSmallPadding),
            )
        }
    }
}
