package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.brios.miempresa.core.ui.theme.AppDimensions

/**
 * Squircle FAB matching Stitch designs (rounded-2xl = 16dp corners).
 * Use for primary creation actions across the app.
 */
@Composable
fun MiEmpresaFAB(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add,
    size: Dp = AppDimensions.mainFABSize,
    iconSize: Dp = AppDimensions.mainFABIconSize,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(bottom = AppDimensions.mediumPadding),
    ) {
        FloatingActionButton(
            modifier = Modifier.size(size),
            onClick = onClick,
            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Icon(
                icon,
                contentDescription,
                Modifier.size(iconSize),
            )
        }
    }
}
