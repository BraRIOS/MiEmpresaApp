package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray500

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .semantics(mergeDescendants = true) {},
        verticalArrangement = verticalArrangement,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StateViewSpotIllustration(icon)

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))

        if (title.isNotEmpty() && title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
        }

        Text(
            modifier = Modifier.padding(horizontal = AppDimensions.largePadding),
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = SlateGray500,
            textAlign = TextAlign.Center,
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

            Button(
                onClick = onAction,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = AppDimensions.largePadding, vertical = AppDimensions.mediumPadding),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            ) {
                if (actionIcon != null) {
                    Icon(
                        actionIcon,
                        contentDescription =
                            if (title.isNotEmpty() && title.isNotBlank()) title else subtitle
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                }
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewPreview() {
    MiEmpresaTheme {
        EmptyStateView(
            icon = Icons.Filled.ShoppingBag,
            title = "Aún no tenés productos",
            subtitle = "Empezá agregando tu primer producto al catálogo",
            actionLabel = "Agregar primer producto",
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewWithButtonIconPreview() {
    MiEmpresaTheme {
        EmptyStateView(
            icon = Icons.Filled.ShoppingBag,
            title = "Aún no tenés productos",
            subtitle = "Empezá agregando tu primer producto al catálogo",
            actionLabel = "Agregar primer producto",
            onAction = {},
            actionIcon = Icons.Filled.AddCircle,
        )
    }
}
