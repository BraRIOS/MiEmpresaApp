package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun MiEmpresaDialog(
    title: String,
    confirmLabel: String,
    dismissLabel: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    icon: ImageVector = Icons.Outlined.Info,
    confirmEnabled: Boolean = true,
    text: String? = null,
    content: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppDimensions.largeIconSize),
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
        },
        text = when {
            content != null -> content
            text != null -> {
                {
                    Text(
                        text,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> null
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled,
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            if (dismissLabel != null)
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors()
                        .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                ) {
                    Text(dismissLabel)
                }
        },
        containerColor = MaterialTheme.colorScheme.background,
    )
}

@Composable
fun DeleteDialog(
    itemName: String,
    title: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    MiEmpresaDialog(
        title = title ?: stringResource(R.string.delete),
        text = stringResource(R.string.delete_dialog_title) + "\"$itemName\"?",
        confirmLabel = stringResource(R.string.delete),
        dismissLabel = stringResource(R.string.cancel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Preview
@Composable
private fun MiEmpresaDialogPreview() {
    MiEmpresaTheme {
        MiEmpresaDialog(
            title = "Eliminar categoría",
            text = "Esta categoría tiene 5 productos asociados. Eliminá los productos primero.",
            confirmLabel = "Eliminar",
            dismissLabel = "Cancelar",
            confirmEnabled = false,
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview
@Composable
private fun DeleteDialogPreview() {
    MiEmpresaTheme {
        DeleteDialog(
            itemName = "Producto re loco",
            title = stringResource(R.string.delete_product),
            onDismiss = {},
            onConfirm = {},
        )
    }
}
