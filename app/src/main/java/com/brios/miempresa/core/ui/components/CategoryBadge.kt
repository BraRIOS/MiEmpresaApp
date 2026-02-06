package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun CategoryBadge(
    emoji: String,
    name: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = remember { RoundedCornerShape(AppDimensions.smallCornerRadius) }

    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        border =
            BorderStroke(
                width = AppDimensions.smallBorderWidth,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = AppDimensions.smallPadding,
                    vertical = AppDimensions.extraSmallPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryBadgePreview() {
    MiEmpresaTheme {
        CategoryBadge(
            emoji = "\uD83C\uDF55",
            name = "Pizzas",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryBadgeNoClickPreview() {
    MiEmpresaTheme {
        CategoryBadge(
            emoji = "\uD83C\uDF55",
            name = "Pizzas",
        )
    }
}
