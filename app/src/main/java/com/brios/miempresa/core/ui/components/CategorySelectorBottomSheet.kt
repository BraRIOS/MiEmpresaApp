package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.brios.miempresa.R
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray100
import com.brios.miempresa.core.ui.theme.SlateGray400

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectorBottomSheet(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showItemCount: Boolean = false,
    productCountByCategory: Map<String, Int> = emptyMap(),
    totalItemCount: Int = 0,
    onCreateCategory: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimensions.mediumLargePadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.categories_count_header,
                        categories.size,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.dismiss),
                    )
                }
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.height(AppDimensions.bottomSheetPeekHeight),
            ) {
                // "Todas" row (only in filter mode)
                if (showItemCount) {
                    item(key = "todas") {
                        CategoryRow(
                            emoji = "📌",
                            name = stringResource(R.string.all_categories_option),
                            itemCount = totalItemCount,
                            showItemCount = true,
                            isSelected = selectedCategoryId == null,
                            onClick = { onCategorySelected(null) },
                        )
                    }
                }

                items(categories, key = { it.id }) { category ->
                    CategoryRow(
                        emoji = category.iconEmoji,
                        name = category.name,
                        itemCount = productCountByCategory[category.id] ?: 0,
                        showItemCount = showItemCount,
                        isSelected = category.id == selectedCategoryId,
                        onClick = { onCategorySelected(category.id) },
                    )
                }
            }

            // "Limpiar filtros" (only when a category is selected in filter mode)
            if (showItemCount && selectedCategoryId != null) {
                TextButton(
                    onClick = { onCategorySelected(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimensions.mediumLargePadding),
                ) {
                    Text(text = stringResource(R.string.clear_filters))
                }
            }

            if (onCreateCategory != null) {
                TextButton(
                    onClick = onCreateCategory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimensions.mediumLargePadding),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = AppDimensions.smallPadding),
                    )
                    Text(text = stringResource(R.string.create_category))
                }
            }

            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(AppDimensions.mediumPadding),
            )
        }
    }
}

@Composable
private fun CategoryRow(
    emoji: String,
    name: String,
    itemCount: Int,
    showItemCount: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = remember { RoundedCornerShape(AppDimensions.smallCornerRadius) }
    val backgroundColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        } else {
            Color.Transparent
        }
    val textColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Surface(
        onClick = onClick,
        shape = shape,
        color = backgroundColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimensions.mediumLargePadding,
                    vertical = AppDimensions.mediumSmallPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
        ) {
            Box(
                modifier = Modifier
                    .size(AppDimensions.largeIconSize)
                    .clip(CircleShape)
                    .background(SlateGray100),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                if (showItemCount) {
                    Text(
                        text = stringResource(R.string.items_count, itemCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGray400,
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private val previewCategories =
    listOf(
        Category(id = "1", name = "Café", iconEmoji = "☕", companyId = "c1"),
        Category(id = "2", name = "Panadería", iconEmoji = "🍞", companyId = "c1"),
        Category(id = "3", name = "Postres", iconEmoji = "🍰", companyId = "c1"),
        Category(id = "4", name = "Bebidas", iconEmoji = "🥤", companyId = "c1"),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CategorySelectorWithSelectionPreview() {
    MiEmpresaTheme {
        CategorySelectorBottomSheet(
            categories = previewCategories,
            selectedCategoryId = "2",
            onCategorySelected = {},
            onDismiss = {},
            showItemCount = true,
            productCountByCategory = mapOf("1" to 5, "2" to 3, "3" to 8, "4" to 2),
            totalItemCount = 18,
            onCreateCategory = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CategorySelectorNoSelectionPreview() {
    MiEmpresaTheme {
        CategorySelectorBottomSheet(
            categories = previewCategories,
            selectedCategoryId = null,
            onCategorySelected = {},
            onDismiss = {},
            showItemCount = true,
            productCountByCategory = mapOf("1" to 5, "2" to 3, "3" to 8, "4" to 2),
            totalItemCount = 18,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CategorySelectorSimplePreview() {
    MiEmpresaTheme {
        CategorySelectorBottomSheet(
            categories = previewCategories,
            selectedCategoryId = "1",
            onCategorySelected = {},
            onDismiss = {},
            onCreateCategory = {},
        )
    }
}
