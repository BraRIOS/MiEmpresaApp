package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions

@Composable
fun CategoryFilterChip(
    selectedCategoryName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        modifier = modifier.width(AppDimensions.categoryFilterChipWidth),
        onClick = onClick,
        label = {
            Text(
                text = selectedCategoryName ?: stringResource(R.string.category),
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingIcon = {
            Icon(
                Icons.Filled.ArrowDropDown,
                stringResource(R.string.category_filter_description),
            )
        },
        colors = AssistChipDefaults.assistChipColors().copy(
            containerColor = if (selectedCategoryName != null) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            },
            labelColor = if (selectedCategoryName != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            trailingIconContentColor = if (selectedCategoryName != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = if (selectedCategoryName != null) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            },
        ),
        shape = ShapeDefaults.Large,
    )
}
