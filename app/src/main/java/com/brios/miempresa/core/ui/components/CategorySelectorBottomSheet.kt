package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    onCreateCategory: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier.navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimensions.mediumLargePadding, vertical = AppDimensions.smallPadding),
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
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(SlateGray100.copy(alpha = 0.5f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.dismiss),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = AppDimensions.smallPadding)
            )

            LazyColumn(
                modifier = Modifier
                    .heightIn(max = AppDimensions.bottomSheetPeekHeight)
                    .padding(horizontal = AppDimensions.smallPadding),
            ) {
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

            // Footer actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (showItemCount && selectedCategoryId != null) {
                    // "Limpiar filtros" button
                    TextButton(
                        onClick = { onCategorySelected(null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimensions.mediumPadding)
                            .height(48.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.clear_filters),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (onCreateCategory != null) {
                    TextButton(
                        onClick = onCreateCategory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimensions.mediumLargePadding, vertical = AppDimensions.mediumPadding),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = AppDimensions.smallPadding),
                        )
                        Text(text = stringResource(R.string.create_category))
                    }
                } else {
                     Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
                }
            }
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
    val shape = CircleShape
    val backgroundColor =
        if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            Color.Transparent
        }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    val textColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(
                horizontal = AppDimensions.mediumPadding,
                vertical = AppDimensions.mediumPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = if (isSelected) 1.dp else 0.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .background(
                    if (isSelected) Color.White else SlateGray100,
                    CircleShape
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (emoji.isNotEmpty() && emoji.isNotBlank())
                Text(
                    text = emoji,
                    fontSize = 20.sp,
                )
            else
                Icon(Icons.Outlined.Sell, contentDescription = name, tint = SlateGray400)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) textColor else MaterialTheme.colorScheme.onSurface,
            )
            if (showItemCount) {
                Text(
                    text = stringResource(R.string.items_count, itemCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) textColor.copy(alpha = 0.8f) else SlateGray400,
                    fontWeight = FontWeight.Medium
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

private val previewCategories =
    listOf(
        Category(id = "1", name = "Café", iconEmoji = "☕", companyId = "c1"),
        Category(id = "2", name = "Panadería", iconEmoji = "🍞", companyId = "c1"),
        Category(id = "3", name = "Sin icono", iconEmoji = "", companyId = "c1"),
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
