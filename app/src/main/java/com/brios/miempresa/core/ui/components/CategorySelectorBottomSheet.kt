package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.brios.miempresa.R
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectorBottomSheet(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onCreateCategory: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppDimensions.mediumLargePadding),
        ) {
            Text(
                text = stringResource(R.string.select_category_placeholder),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(AppDimensions.mediumSmallPadding))
            LazyColumn(
                modifier = Modifier.height(AppDimensions.bottomSheetPeekHeight),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        isSelected = category.id == selectedCategoryId,
                        onClick = { onCategorySelected(category.id) },
                    )
                }
            }
            if (onCreateCategory != null) {
                TextButton(
                    onClick = onCreateCategory,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = AppDimensions.smallPadding),
                    )
                    Text(text = stringResource(R.string.create_category))
                }
            }
            Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = remember { RoundedCornerShape(AppDimensions.smallCornerRadius) }
    val backgroundColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        }
    val border =
        if (isSelected) {
            BorderStroke(AppDimensions.mediumBorderWidth, MaterialTheme.colorScheme.primary)
        } else {
            null
        }

    Surface(
        onClick = onClick,
        shape = shape,
        color = backgroundColor,
        border = border,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppDimensions.mediumSmallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
        ) {
            Text(
                text = category.iconEmoji,
                fontSize = 24.sp,
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
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
        )
    }
}
