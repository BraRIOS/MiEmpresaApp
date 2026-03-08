package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray500

@Composable
fun NotFoundView(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.SearchOff,
    message: String,
    actionLabel: String = stringResource(R.string.clear_filters),
    onAction: (() -> Unit)? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics(mergeDescendants = true) {},
        verticalArrangement = verticalArrangement,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StateViewSpotIllustration(
            icon = icon,
            containerSize = 100.dp,
            iconSize = 48.dp,
            iconTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        Text(
            modifier = Modifier.padding(horizontal = AppDimensions.largePadding),
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = SlateGray500,
            textAlign = TextAlign.Center,
        )

        if (onAction != null) {
            Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotFoundViewPreview() {
    MiEmpresaTheme {
        NotFoundView(
            message = "No se encontraron productos",
            onAction = {},
        )
    }
}
