package com.brios.miempresa.categories.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.emoji2.emojipicker.EmojiPickerView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray400

private val QuickPickEmojis = listOf("🍔", "🥤", "👕", "🏠", "📦", "📱")

@Composable
fun CategoryFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryFormViewModel = hiltViewModel(),
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val selectedEmoji by viewModel.selectedEmoji.collectAsStateWithLifecycle()
    val nameError by viewModel.nameError.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val productCount by viewModel.productCount.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.saveComplete.collect { onNavigateBack() }
    }

    CategoryFormContent(
        name = name,
        selectedEmoji = selectedEmoji,
        nameError = nameError,
        isSaving = isSaving,
        isEditMode = viewModel.isEditMode,
        productCount = productCount,
        onNameChanged = viewModel::onNameChanged,
        onEmojiSelected = viewModel::onEmojiSelected,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFormContent(
    name: String,
    selectedEmoji: String,
    nameError: String?,
    isSaving: Boolean,
    isEditMode: Boolean,
    productCount: Int,
    onNameChanged: (String) -> Unit,
    onEmojiSelected: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEmojiBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) {
                            stringResource(R.string.edit_category)
                        } else {
                            stringResource(R.string.add_category)
                        },
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 8.dp,
            ) {
                Button(
                    onClick = onSave,
                    enabled = name.isNotBlank() && !isSaving,
                    modifier = Modifier
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
                        text = stringResource(R.string.save_category),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AppDimensions.mediumLargePadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.largePadding),
        ) {
            Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))

            IntegratedNameField(
                name = name,
                selectedEmoji = selectedEmoji,
                nameError = nameError,
                onNameChanged = onNameChanged,
            )

            EmojiQuickPickSection(
                selectedEmoji = selectedEmoji,
                onEmojiSelected = onEmojiSelected,
                onShowAllEmojis = { showEmojiBottomSheet = true },
            )

            InfoCard()
        }

        if (showEmojiBottomSheet) {
            EmojiDialog(
                onEmojiSelected = { emoji ->
                    onEmojiSelected(emoji)
                    showEmojiBottomSheet = false
                },
                onDismiss = { showEmojiBottomSheet = false },
            )
        }

        if (showDeleteDialog) {
            DeleteCategoryDialog(
                productCount = productCount,
                onConfirm = {
                    showDeleteDialog = false
                    onDelete()
                },
                onDismiss = { showDeleteDialog = false },
            )
        }
    }
}

@Composable
private fun IntegratedNameField(
    name: String,
    selectedEmoji: String,
    nameError: String?,
    onNameChanged: (String) -> Unit,
) {
    Column {
        Card(
            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border =
                if (nameError != null) {
                    BorderStroke(AppDimensions.mediumBorderWidth, MaterialTheme.colorScheme.error)
                } else {
                    BorderStroke(AppDimensions.smallBorderWidth, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left emoji section (72dp)
                Box(
                    modifier =
                        Modifier
                            .width(AppDimensions.Categories.emojiPreviewWidth)
                            .height(AppDimensions.Categories.emojiPreviewHeight)
                            .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selectedEmoji.isNotEmpty()) {
                        Text(
                            text = selectedEmoji,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(AppDimensions.mediumIconSize),
                        )
                    }
                }

                // Right TextField section
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(AppDimensions.mediumPadding),
                ) {
                    Text(
                        text = "NOMBRE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextField(
                        value = name,
                        onValueChange = onNameChanged,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.category_name_placeholder),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            )
                        },
                        textStyle =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        singleLine = true,
                        colors =
                            TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Helper text
        Text(
            text =
                nameError ?: stringResource(R.string.category_name_helper),
            style = MaterialTheme.typography.bodySmall,
            color =
                if (nameError != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    SlateGray400
                },
            modifier =
                Modifier.padding(
                    start = AppDimensions.smallPadding,
                    top = AppDimensions.smallPadding,
                ),
        )
    }
}

@Composable
private fun EmojiQuickPickSection(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    onShowAllEmojis: () -> Unit,
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimensions.extraSmallPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.icon_label),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onShowAllEmojis) {
                Text(
                    text = stringResource(R.string.see_all_emojis),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Card(
            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(AppDimensions.mediumLargePadding),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
            ) {
                QuickPickEmojis.forEach { emoji ->
                    val isSelected = emoji == selectedEmoji
                    Surface(
                        onClick = { onEmojiSelected(emoji) },
                        modifier =
                            Modifier
                                .size(AppDimensions.categoryEmojiContainerSize),
                        shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            },
                        border =
                            if (isSelected) {
                                BorderStroke(
                                    AppDimensions.mediumBorderWidth,
                                    MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                null
                            },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }
                }

                // "+" button to open all emojis
                val borderColor = MaterialTheme.colorScheme.outlineVariant
                val borderWidth = AppDimensions.mediumBorderWidth
                val dashWidth = 4.dp
                val dashGap = 2.dp

                OutlinedButton(
                    onClick = onShowAllEmojis,
                    modifier = Modifier
                        .size(AppDimensions.categoryEmojiContainerSize)
                        .drawWithContent {
                            drawContent()
                            drawOutline(
                                outline = RoundedCornerShape(AppDimensions.mediumCornerRadius).createOutline(size, layoutDirection, this),
                                style = Stroke(
                                    width = borderWidth.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(dashWidth.toPx(), dashGap.toPx())
                                    )
                                ),
                                brush = SolidColor(borderColor)
                            )
                        },
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    contentPadding = PaddingValues(0.dp),
                    border = null,
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = SlateGray400,
                        ),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.see_all_emojis),
                        modifier = Modifier.size(AppDimensions.defaultIconSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    val infoBg = Color(0xFFEFF6FF) // blue-50
    val infoBorder = Color(0xFFDBEAFE) // blue-100
    val infoIcon = Color(0xFF3B82F6) // blue-500
    val infoText = Color(0xFF2563EB) // blue-600

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = infoBg,
                    shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
                )
                .border(
                    AppDimensions.smallBorderWidth,
                    infoBorder,
                    RoundedCornerShape(AppDimensions.smallCornerRadius),
                )
                .padding(AppDimensions.mediumSmallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = infoIcon,
            modifier = Modifier.size(AppDimensions.smallIconSize),
        )
        Text(
            text = stringResource(R.string.emoji_info_hint),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = infoText,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmojiDialog(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.select_emoji_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.dismiss))
                        }
                    }
                )

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { context ->
                        EmojiPickerView(context).apply {
                            setOnEmojiPickedListener { emojiViewItem ->
                                onEmojiSelected(emojiViewItem.emoji)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DeleteCategoryDialog(
    productCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_category)) },
        text = {
            if (productCount > 0) {
                Text(stringResource(R.string.category_has_products, productCount))
            } else {
                Text(stringResource(R.string.confirm_delete_category))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = productCount == 0,
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true, name = "Edit Category")
@Composable
private fun CategoryFormScreenPreview() {
    MiEmpresaTheme {
        CategoryFormContent(
            name = "Bebidas",
            selectedEmoji = "🥤",
            nameError = null,
            isSaving = false,
            isEditMode = true,
            productCount = 5,
            onNameChanged = {},
            onEmojiSelected = {},
            onSave = {},
            onDelete = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "New Category")
@Composable
private fun NewCategoryPreview() {
    MiEmpresaTheme {
        CategoryFormContent(
            name = "",
            selectedEmoji = "",
            nameError = null,
            isSaving = false,
            isEditMode = false,
            productCount = 0,
            onNameChanged = {},
            onEmojiSelected = {},
            onSave = {},
            onDelete = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Category No Emoji")
@Composable
private fun EditCategoryNoEmojiPreview() {
    MiEmpresaTheme {
        CategoryFormContent(
            name = "Sin Emoji",
            selectedEmoji = "",
            nameError = null,
            isSaving = false,
            isEditMode = true,
            productCount = 0,
            onNameChanged = {},
            onEmojiSelected = {},
            onSave = {},
            onDelete = {},
            onNavigateBack = {}
        )
    }
}
