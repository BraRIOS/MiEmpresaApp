package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun CategoryBadge(
    emoji: String,
    name: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(AppDimensions.smallCornerRadius)
    Surface(
        modifier =
            modifier
                .border(
                    width = AppDimensions.smallBorderWidth,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = shape,
                )
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
                ),
        shape = shape,
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = emoji,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun CategoryBadgePreview() {
    MiEmpresaTheme {
        CategoryBadge(
            emoji = "🍕",
            name = "Pizzas",
            onClick = {},
        )
    }
}
