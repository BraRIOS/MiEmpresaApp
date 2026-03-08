package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        ) {
            BadgeContent(emoji = emoji, name = name, textColor = MaterialTheme.colorScheme.primary)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
        ) {
            BadgeContent(emoji = emoji, name = name, textColor = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun BadgeContent(
    emoji: String,
    name: String,
    textColor: Color
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
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryBadgePreview() {
    MiEmpresaTheme {
        Surface(
            modifier = Modifier.background(Color.White),
            color = MaterialTheme.colorScheme.background,
        ) {
            CategoryBadge(
                emoji = "☕",
                name = "Café",
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryBadgeNoClickPreview() {
    MiEmpresaTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            CategoryBadge(
                emoji = "☕",
                name = "Café",
            )
        }
    }
}
