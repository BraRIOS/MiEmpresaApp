package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray700

@Composable
fun CategoryBadge(
    emoji: String,
    name: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = remember { RoundedCornerShape(AppDimensions.smallCornerRadius) }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            BadgeContent(emoji = emoji, name = name)
        }
    } else {
        Row(
            modifier =
                modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = shape,
                    )
                    .padding(
                        horizontal = AppDimensions.smallPadding,
                        vertical = AppDimensions.extraSmallPadding,
                    ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
        ) {
            if (emoji.isNotEmpty()) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Text(
                text = name.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SlateGray700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BadgeContent(
    emoji: String,
    name: String,
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
        if (emoji.isNotEmpty()) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = SlateGray700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryBadgePreview() {
    MiEmpresaTheme {
        CategoryBadge(
            emoji = "☕",
            name = "Café",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryBadgeNoClickPreview() {
    MiEmpresaTheme {
        CategoryBadge(
            emoji = "☕",
            name = "Café",
        )
    }
}
